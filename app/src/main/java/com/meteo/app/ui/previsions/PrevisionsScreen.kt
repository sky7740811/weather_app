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
        SearchBar(viewModel)

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
fun SearchBar(viewModel: MainViewModel) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Rechercher une ville...", color = Muted) },
            modifier = Modifier.weight(1f).clickable { viewModel.setSearchMode(true) },
            singleLine = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = CardBorder,
                disabledTextColor = Text,
                disabledPlaceholderColor = Muted
            ),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = { viewModel.setSearchMode(true) },
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
    val (canicule, nuitTropicale) = generateAlerts(daysMap)

    LazyColumn(Modifier.fillMaxSize()) {
        // Alert box
        if (canicule.isNotEmpty() || nuitTropicale.isNotEmpty()) {
            item { AlertBox(canicule, nuitTropicale) }
        }

        // Day cards
        val sorted = daysMap.entries.sortedBy { it.key }
        items(sorted) { (date, hourList) ->
            DayCard(date, hourList, city)
        }
    }
}

fun generateAlerts(daysMap: Map<String, List<HourData>>): Pair<List<String>, List<String>> {
    val dayHours = 10..20
    val nightHours = (21..23).toList() + (0..9).toList()
    val canicule = mutableListOf<String>()
    val nuitTropicale = mutableListOf<String>()

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
                canicule.add("$dt/$mo ($dayName) ${hot.first().hour}h~${hot.last().hour}h")
            }
        }
        if (nightMin <= 21 && hours.any { it.hour in nightHours && it.temp >= 21 }) {
            val dt = date.substringAfterLast("-").toIntOrNull() ?: 0
            val mo = date.split("-")[1].toIntOrNull() ?: 0
            val parsed = sdf.parse(date) ?: continue
            val cal = Calendar.getInstance().apply { time = parsed }
            val dayName = WeatherUtil.DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
            nuitTropicale.add("$dt/$mo ($dayName)")
        }
    }
    return canicule to nuitTropicale
}

@Composable
fun AlertBox(canicule: List<String>, nuitTropicale: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val total = canicule.size + nuitTropicale.size
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0F0F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DE74C3C)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp).clickable { expanded = !expanded }) {
            Text("⚠️ $total alerte(s)", color = Hot, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x4DE74C3C))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    if (canicule.isNotEmpty()) {
                        Column(Modifier.weight(1f)) {
                            Text("Alert Canicule", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Hot)
                            Spacer(Modifier.height(4.dp))
                            canicule.forEach { Text(it, fontSize = 11.sp, color = Hot) }
                        }
                    }
                    if (nuitTropicale.isNotEmpty()) {
                        Column(Modifier.weight(1f)) {
                            Text("Alert Nuit Tropicale", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Hot)
                            Spacer(Modifier.height(4.dp))
                            nuitTropicale.forEach { Text(it, fontSize = 11.sp, color = Hot) }
                        }
                    }
                }
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
                    HourCell(h, date)
                }
            }
        }
    }
}

@Composable
fun HourCell(h: HourData, date: String) {
    var showPopup by remember { mutableStateOf(false) }
    val bg = Color(WeatherUtil.tempColor(h.temp.toDouble()))
    val ico = WeatherUtil.weatherIcon(h.weatherCode)
    val uvColor = h.uv?.let { WeatherUtil.uvLevel(it).color } ?: 0x00000000
    val uvInfo = h.uv?.let { WeatherUtil.uvLevel(it) }

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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16162A)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(Modifier.padding(0.dp)) {
                        Row(
                            Modifier.fillMaxWidth().background(bg, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ico, fontSize = 32.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${h.temp}°C", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color.White)
                                Text(WeatherUtil.weatherDesc(h.weatherCode), fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
                        Column(Modifier.padding(14.dp)) {
                            InfoRow("Date", date, Accent)
                            InfoRow("Heure", "${h.hour}h", Accent2)
                            if (uvInfo != null) {
                                val spfTag = if (uvInfo.spf.isNotEmpty()) " [${uvInfo.spf}]" else ""
                                InfoRow("UV", "${h.uv} — ${uvInfo.label}$spfTag", Color(uvInfo.color))
                            }
                            if (h.rain != null) InfoRow("Pluie", "${h.rain}%", Cool)
                            if (h.cloud != null) InfoRow("Nuages", "${h.cloud}%", Muted)
                            if (h.wind != null) InfoRow("Vent", "${h.wind} km/h", Accent2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Text) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Muted)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}
