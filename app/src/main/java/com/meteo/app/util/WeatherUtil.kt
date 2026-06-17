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
    fun uvLevel(uv: Double): UVInfo = when {
        uv <= 2 -> UVInfo("Faible", "", 0xFF00E676)
        uv <= 5 -> UVInfo("Modéré", "SPF30", 0xFFFFD600)
        uv <= 7 -> UVInfo("Élevé", "SPF30+", 0xFFFF6D00)
        uv <= 10 -> UVInfo("Très élevé", "SPF50", 0xFFFF1744)
        else -> UVInfo("Extrême", "SPF50+ obligatoire", 0xFFD500F9)
    }

    fun tempColor(t: Double): Long = when {
        t >= 40 -> 0xFF8B1A1A
        t >= 37 -> 0xFFB33030
        t >= 35 -> 0xFFC07030
        t >= 33 -> 0xFFD49040
        t >= 30 -> 0xFFD4A030
        t >= 28 -> 0xFF60A040
        t >= 25 -> 0xFF408050
        t >= 22 -> 0xFF307080
        t >= 18 -> 0xFF306090
        t >= 14 -> 0xFF204878
        else -> 0xFF183060
    }

    val DAYS = listOf("dim", "lun", "mar", "mer", "jeu", "ven", "sam")
    val DAYS_CAP = listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
    val MONTHS = listOf("janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre")
}
