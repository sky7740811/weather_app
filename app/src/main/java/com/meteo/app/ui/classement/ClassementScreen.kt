package com.meteo.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.data.model.City
import com.meteo.app.ui.theme.*
import com.meteo.app.util.WeatherUtil
import java.util.*

@Composable
fun ClassementScreen(viewModel: MainViewModel) {
    val classement by viewModel.classement.collectAsState()
    val loading by viewModel.classLoading.collectAsState()

    // Date buttons
    val today = Calendar.getInstance()
    val dateList = (0 until 7).map { d ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, d) }
        val mm = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val dd = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
        val yyyy = cal.get(Calendar.YEAR)
        "$yyyy-$mm-$dd" to "${dd}/${mm} ${WeatherUtil.DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
    }

    var selectedDate by remember { mutableStateOf(dateList.first().first) }

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("Classement des max", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(6.dp))

        // Date bar
        LazyRow(Modifier.fillMaxWidth()) {
            items(dateList) { (dateStr, label) ->
                val active = dateStr == selectedDate
                Button(
                    onClick = {
                        selectedDate = dateStr
                        viewModel.loadClassement(dateStr)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) Accent else Card,
                        contentColor = if (active) Text else Muted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) { Text(label, fontSize = 11.sp) }
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            else -> {
                // Table header
                Card(
                    colors = CardDefaults.cardColors(containerColor = Card),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text("#", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                            Text("", modifier = Modifier.width(28.dp))
                            Text("Ville", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("°C", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(36.dp))
                        }
                        HorizontalDivider(color = CardBorder)
                        classement.forEachIndexed { rank, (temp, code) ->
                            if (temp != null) {
                                val name = City.RANK_CITIES.getOrNull(rank)?.name ?: "?"
                                val tcolor = when {
                                    temp >= 37 -> Hot; temp >= 35 -> Warm; temp >= 30 -> Cool; else -> Cold
                                }
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp).clickable { },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${rank + 1}", color = Muted, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                                    Text(WeatherUtil.weatherIcon(code?.toInt()), fontSize = 14.sp, modifier = Modifier.width(28.dp))
                                    Text(name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text("${temp.toInt()}°", color = tcolor, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(36.dp))
                                }
                                if (rank < classement.size - 1) HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}
