package com.meteo.app.data.model

import com.google.gson.annotations.SerializedName

// Open-Meteo API response
data class ForecastResponse(
    val hourly: HourlyData
)

data class HourlyData(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperature2m: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("uv_index") val uvIndex: List<Double>?,
    @SerializedName("precipitation_probability") val precipProb: List<Int>?,
    @SerializedName("cloud_cover") val cloudCover: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double>?
)

// Classement multi-city
data class MultiForecastResponse(
    val daily: DailyData?
)

data class DailyData(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?
)

// Geocoding
data class GeoResponse(
    val features: List<GeoFeature>?
)

data class GeoFeature(
    val properties: GeoProperties,
    val geometry: GeoGeometry
)

data class GeoProperties(
    val name: String,
    val postcode: String?,
    val context: String?
)

data class GeoGeometry(
    val coordinates: List<Double>
)
