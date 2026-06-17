package com.meteo.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.data.model.City
import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.data.model.HourData
import com.meteo.app.ui.theme.*
import com.meteo.app.util.WeatherUtil
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrevisionsScreen(viewModel: MainViewModel) {
    val forecast by viewModel.forecast.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val city by viewModel.city.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Search bar
        SearchBar(onSearch = { viewModel.searchCity(it) })

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "", color = Danger)
            }
            forecast != null -> ForecastContent(forecast!!, city)
        }
    }
}

@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Ville...", color = Muted) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = Text,
                unfocusedTextColor = Text,
                cursorColor = Accent
            ),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                if (query.isNotBlank()) {
                    onSearch(query.trim())
                    query = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(8.dp)
        ) { Text("OK") }
    }
}

@Composable
fun ForecastContent(data: ForecastResponse, city: City) {
    val hours = data.hourly
    val size = hours.time.size
    if (size == 0) return

    // Group by day
    data class DayData(val date: String, val hours: List<HourData>)
    val daysMap = mutableMapOf<String, MutableList<HourData>>()
    for (i in 0 until size) {
        val date = hours.time[i].substringBefore("T")
        val h = hours.time[i].substringAfter("T").substringBefore(":").toIntOrNull() ?: continue
        val t = hours.temperature2m[i]
        val wc = hours.weatherCode?.getOrNull(i)
        val uv = hours.uvIndex?.getOrNull(i)
        val rn = hours.precipProb?.getOrNull(i)?.toDouble()
        val cl = hours.cloudCover?.getOrNull(i)?.toDouble()
        val wd = hours.windSpeed?.getOrNull(i)
        daysMap.getOrPut(date) { mutableListOf() }
            .add(HourData(h, t.toInt(), wc, uv, rn?.toInt(), cl?.toInt(), wd))
    }

    // Alerts
    val alerts = generateAlerts(daysMap)

    LazyColumn(Modifier.fillMaxSize()) {
        // Alert box
        if (alerts.isNotEmpty()) {
            item { AlertBox(alerts) }
        }

        // Day cards
        val sorted = daysMap.entries.sortedBy { it.key }
        items(sorted) { (date, hourList) ->
            DayCard(date, hourList, city)
        }
    }
}

fun generateAlerts(daysMap: Map<String, List<HourData>>): List<String> {
    val dayHours = 10..20
    val nightHours = (21..23).toList() + (0..9).toList()
    val alerts = mutableListOf<String>()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    val sorted = daysMap.entries.sortedBy { it.key }
    for ((date, hours) in sorted) {
        val dayMax = hours.filter { it.hour in dayHours }.maxOfOrNull { it.temp } ?: 0
        val nightMin = hours.filter { it.hour in nightHours }.minOfOrNull { it.temp } ?: 99

        if (dayMax >= 36) {
            val hot = hours.filter { it.hour in dayHours && it.temp >= 36 }
            if (hot.isNotEmpty()) {
                val dt = date.substringAfterLast("-").toIntOrNull() ?: 0
                val mo = date.split("-")[1].toIntOrNull() ?: 0
                val parsed = sdf.parse(date) ?: continue
                val cal = Calendar.getInstance().apply { time = parsed }
                val dayName = WeatherUtil.DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
                alerts.add("☀️ $dt/$mo ($dayName) ${hot.first().hour}h~${hot.last().hour}h")
            }
        }
        if (nightMin <= 21 && hours.any { it.hour in nightHours && it.temp >= 21 }) {
            val dt = date.substringAfterLast("-").toIntOrNull() ?: 0
            val mo = date.split("-")[1].toIntOrNull() ?: 0
            val parsed = sdf.parse(date) ?: continue
            val cal = Calendar.getInstance().apply { time = parsed }
            val dayName = WeatherUtil.DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
            alerts.add("🌙 $dt/$mo ($dayName)")
        }
    }
    return alerts
}

@Composable
fun AlertBox(alerts: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0F0F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DE74C3C)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp).clickable { expanded = !expanded }) {
            Text("⚠️ ${alerts.size} alerte(s)", color = Hot, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x4DE74C3C))
                Spacer(Modifier.height(6.dp))
                alerts.forEach { Text(it, color = Hot, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun DayCard(date: String, hours: List<HourData>, city: City) {
    val maxT = hours.maxOfOrNull { it.temp } ?: 0
    val minT = hours.minOfOrNull { it.temp } ?: 0
    val dt = try { SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).parse(date) } catch (e: Exception) { null }
    val cal = dt?.let { Calendar.getInstance().apply { time = it } }
    val label = if (cal != null) {
        "${WeatherUtil.DAYS_CAP[cal.get(Calendar.DAY_OF_WEEK) - 1]} ${cal.get(Calendar.DAY_OF_MONTH)} ${WeatherUtil.MONTHS[cal.get(Calendar.MONTH)]}"
    } else date

    Card(
        colors = CardDefaults.cardColors(containerColor = Card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("$maxT° / $minT°", color = Hot, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))

            // Hourly scroll
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                hours.sortedBy { it.hour }.forEach { h ->
                    HourCell(h)
                }
            }
        }
    }
}

@Composable
fun HourCell(h: HourData) {
    var showPopup by remember { mutableStateOf(false) }
    val bg = Color(WeatherUtil.tempColor(h.temp.toDouble()))
    val ico = WeatherUtil.weatherIcon(h.weatherCode)
    val uvColor = h.uv?.let { WeatherUtil.uvLevel(it).color } ?: 0x00000000

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(40.dp)
                .padding(horizontal = 1.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .clickable { showPopup = true }
                .padding(vertical = 4.dp)
        ) {
            Text("${h.hour}h", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            if (ico.isNotEmpty()) Text(ico, fontSize = 12.sp)
            Text("${h.temp}°", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            if (uvColor != 0x00000000L) {
                Box(Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 8.dp).background(Color(uvColor), RoundedCornerShape(2.dp)))
            }
        }

        if (showPopup) {
            Popup(
                onDismissRequest = { showPopup = false },
                alignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Card),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${h.hour}h - ${h.temp}°C", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(WeatherUtil.weatherDesc(h.weatherCode), fontSize = 13.sp)
                        if (h.uv != null) Text("UV: ${h.uv}", fontSize = 13.sp)
                        if (h.rain != null) Text("Pluie: ${h.rain}%", fontSize = 13.sp)
                        if (h.cloud != null) Text("Nuages: ${h.cloud}%", fontSize = 13.sp)
                        if (h.wind != null) Text("Vent: ${h.wind} km/h", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
