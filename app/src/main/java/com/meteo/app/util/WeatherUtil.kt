package com.meteo.app.util

object WeatherUtil {
    fun weatherIcon(code: Int?): String = when (code) {
        0, 1 -> "☀️"
        2 -> "⛅"
        3 -> "☁️"
        in 45..48 -> "🌫️"
        in 51..57 -> "🌦️"
        in 61..65 -> "🌧️"
        in 71..77 -> "❄️"
        in 80..82 -> "🌦️"
        in 85..86 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> ""
    }

    fun weatherDesc(code: Int?): String = when (code) {
        0, 1 -> "Ciel dégagé"
        2 -> "Partiellement nuageux"
        3 -> "Nuageux"
        in 45..48 -> "Brouillard"
        in 51..57 -> "Bruine"
        in 61..65 -> "Pluie"
        in 71..77 -> "Neige"
        in 80..82 -> "Averses"
        in 85..86 -> "Averses de neige"
        95, 96, 99 -> "Orage"
        else -> ""
    }

    data class UVInfo(val label: String, val spf: String, val color: Long)
    fun uvLevel(val: Double): UVInfo = when {
        val <= 2 -> UVInfo("Faible", "", 0xFF00E676)
        val <= 5 -> UVInfo("Modéré", "SPF30", 0xFFFFD600)
        val <= 7 -> UVInfo("Élevé", "SPF30+", 0xFFFF6D00)
        val <= 10 -> UVInfo("Très élevé", "SPF50", 0xFFFF1744)
        else -> UVInfo("Extrême", "SPF50+ obligatoire", 0xFFD500F9)
    }

    fun tempColor(t: Double): Long = when {
        t >= 40 -> 0xFF6B0000
        t >= 37 -> 0xFF8B0000
        t >= 35 -> 0xFFC0392B
        t >= 33 -> 0xFFD35400
        t >= 30 -> 0xFFE67E22
        t >= 28 -> 0xFFF39C12
        t >= 25 -> 0xFF27AE60
        t >= 22 -> 0xFF55EFC4
        t >= 18 -> 0xFF3498DB
        t >= 14 -> 0xFF2980B9
        else -> 0xFF1A5276
    }

    val DAYS = listOf("dim", "lun", "mar", "mer", "jeu", "ven", "sam")
    val DAYS_CAP = listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
    val MONTHS = listOf("janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre")
}
