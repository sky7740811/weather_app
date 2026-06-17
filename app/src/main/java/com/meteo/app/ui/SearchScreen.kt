package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.ui.theme.*

@Composable
fun SearchScreen(viewModel: MainViewModel) {
    var query by remember { mutableStateOf("") }
    val suggestions by viewModel.suggestions.collectAsState()

    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchSuggestions(it)
                },
                placeholder = { Text("Rechercher une ville...", color = Muted) },
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
            TextButton(onClick = { viewModel.setSearchMode(false) }) {
                Text("Annuler", color = Muted)
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(suggestions) { f ->
                val p = f.properties
                val label = buildString {
                    append(p.name)
                    if (p.postcode != null) append(" (${p.postcode})")
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Card),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                        viewModel.selectSuggestion(f)
                        query = ""
                        viewModel.setSearchMode(false)
                    }
                ) {
                    Text(label, modifier = Modifier.padding(14.dp), fontSize = 14.sp, color = Text, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
