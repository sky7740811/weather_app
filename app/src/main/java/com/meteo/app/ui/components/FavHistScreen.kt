package com.meteo.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.ui.theme.*

@Composable
fun FavoritesScreen(viewModel: MainViewModel) {
    val prefs = viewModel.prefs
    var favs by remember { mutableStateOf(prefs.getFavs()) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("⭐ Favoris", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))

        if (favs.isEmpty()) {
            Text("Aucun favori", color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            favs.forEachIndexed { i, city ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectCity(city) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(city.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            listOfNotNull(
                                city.dept.takeIf { it.isNotEmpty() },
                                city.postcode.takeIf { it.isNotEmpty() }
                            ).joinToString(" · "),
                            color = Muted, fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = {
                        val list = prefs.getFavs().toMutableList()
                        list.removeAt(i)
                        prefs.saveFavs(list)
                        favs = prefs.getFavs()
                    }) { Text("✕", color = Danger) }
                }
                if (i < favs.size - 1) Divider(color = CardBorder.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val prefs = viewModel.prefs
    var hist by remember { mutableStateOf(prefs.getHist()) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("🕐 Historique", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))

        if (hist.isEmpty()) {
            Text("Aucun historique", color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            hist.forEachIndexed { i, city ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectCity(city) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(city.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(city.dept.takeIf { it.isNotEmpty() } ?: "", color = Muted, fontSize = 12.sp)
                    }
                    TextButton(onClick = {
                        val list = prefs.getHist().toMutableList()
                        list.removeAt(i)
                        prefs.saveHist(list)
                        hist = prefs.getHist()
                    }) { Text("✕", color = Danger) }
                }
                if (i < hist.size - 1) Divider(color = CardBorder.copy(alpha = 0.3f))
            }
        }
    }
}
