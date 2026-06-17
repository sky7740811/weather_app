package com.meteo.app.data.api

import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.data.model.GeoResponse
import com.meteo.app.data.model.MultiForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeteoApi {

    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m,weather_code,uv_index,precipitation_probability,cloud_cover,wind_speed_10m",
        @Query("timezone") tz: String = "Europe/Paris",
        @Query("forecast_days") days: Int = 14
    ): ForecastResponse

    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getMultiForecast(
        @Query("latitude") lats: String,
        @Query("longitude") lons: String,
        @Query("daily") daily: String = "temperature_2m_max,weather_code",
        @Query("timezone") tz: String = "Europe/Paris",
        @Query("forecast_days") days: Int = 14
    ): List<MultiForecastResponse>

    @GET("https://api-adresse.data.gouv.fr/search/")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8,
        @Query("type") type: String = "municipality"
    ): GeoResponse
}
