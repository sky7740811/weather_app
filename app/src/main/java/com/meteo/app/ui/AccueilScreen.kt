package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.ui.theme.*
import com.meteo.app.util.WeatherUtil
import java.util.Calendar

@Composable
fun AccueilScreen(viewModel: MainViewModel) {
    val forecast by viewModel.forecast.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val city by viewModel.city.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        if (loading && forecast == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (error != null && forecast == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "", color = Danger)
            }
        } else if (forecast != null) {
            AccueilContent(forecast!!, city.name)
        }
    }
}

@Composable
fun AccueilContent(data: ForecastResponse, cityName: String) {
    val hours = data.hourly
    val now = Calendar.getInstance()
    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    val todayStr = String.format("%04d-%02d-%02d", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))

    val todayHours = mutableListOf<Int>()
    for (i in hours.time.indices) {
        val d = hours.time[i].substringBefore("T")
        val h = hours.time[i].substringAfter("T").substringBefore(":").toIntOrNull() ?: continue
        if (d == todayStr) todayHours.add(i)
    }

    val currentIdx = todayHours.firstOrNull { i ->
        val h = hours.time[i].substringAfter("T").substringBefore(":").toIntOrNull()
        h == currentHour
    }

    val nowIdx = currentIdx ?: (todayHours.lastOrNull() ?: 0)

    val temp = hours.temperature2m.getOrNull(nowIdx)?.toInt() ?: return
    val wc = hours.weatherCode?.getOrNull(nowIdx)
    val uv = hours.uvIndex?.getOrNull(nowIdx)
    val rain = hours.precipProb?.getOrNull(nowIdx)
    val cloud = hours.cloudCover?.getOrNull(nowIdx)
    val wind = hours.windSpeed?.getOrNull(nowIdx)

    val ico = WeatherUtil.weatherIcon(wc)
    val desc = WeatherUtil.weatherDesc(wc)
    val uvInfo = uv?.let { WeatherUtil.uvLevel(it) }

    val temps = todayHours.mapNotNull { i -> hours.temperature2m.getOrNull(i)?.toInt() }
    val maxToday = temps.maxOrNull() ?: 0
    val minToday = temps.minOrNull() ?: 0

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        Text(cityName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Text)

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Card),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(ico, fontSize = 56.sp)
                Text("$temp°C", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = Text)
                Text(desc, fontSize = 16.sp, color = Text.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Text("Max $maxToday° / Min $minToday°", fontSize = 14.sp, color = Muted)
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Card),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("💨 Vent", "${wind?.toInt() ?: "-"} km/h")
                DetailRow("💧 Pluie", "${rain ?: "-"}%")
                DetailRow("☁️ Nuages", "${cloud ?: "-"}%")
                if (uvInfo != null) {
                    DetailRow("☀️ UV", "${uv} — ${uvInfo.label}")
                    Spacer(Modifier.height(2.dp))
                    Text("  ${uvInfo.spf}", fontSize = 12.sp, color = Color(uvInfo.color))
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Muted)
        Text(value, fontSize = 14.sp, color = Text, fontWeight = FontWeight.Medium)
    }
}
