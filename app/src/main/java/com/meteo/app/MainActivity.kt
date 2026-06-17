package com.meteo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.meteo.app.ui.theme.MeteoTheme
import com.meteo.app.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeteoTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}
