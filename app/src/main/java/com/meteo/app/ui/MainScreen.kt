package com.meteo.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meteo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val tab by viewModel.tab.collectAsState()
    val city by viewModel.city.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = city.name + if (city.postcode.isNotEmpty()) " (${city.postcode})" else "",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    val isFav by viewModel.isFav.collectAsState()
                    val canRefresh by viewModel.canRefresh.collectAsState()
                    IconButton(onClick = { viewModel.loadForecast() }, enabled = canRefresh) {
                        Text("↻", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { viewModel.toggleFav() }) {
                        Text(if (isFav) "★" else "☆", style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        bottomBar = {
            BottomNavBar(tab) { viewModel.setTab(it) }
        },
        containerColor = Bg
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> PrevisionsScreen(viewModel)
                1 -> ClassementScreen(viewModel)
                2 -> FavoritesScreen(viewModel)
                3 -> HistoryScreen(viewModel)
            }
        }
    }
}

@Composable
fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = Card, tonalElevation = 0.dp) {
        val items = listOf("☀️ Prévisions", "🏆 Classement", "⭐ Favoris", "🕐 Historique")
        items.forEachIndexed { i, label ->
            NavigationBarItem(
                selected = selected == i,
                onClick = { onSelect(i) },
                icon = { Text(label.split(" ")[0], fontSize = MaterialTheme.typography.titleMedium.fontSize) },
                label = { Text(label.split(" ").getOrElse(1) { "" }, fontSize = MaterialTheme.typography.labelSmall.fontSize) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    indicatorColor = CardBorder
                )
            )
        }
    }
}
