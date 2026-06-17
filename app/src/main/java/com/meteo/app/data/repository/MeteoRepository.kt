package com.meteo.app.data.repository

import com.meteo.app.data.api.MeteoApi
import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.data.model.GeoResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MeteoRepository {

    private val api: MeteoApi

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        api = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.open-meteo.com/")
            .build()
            .create(MeteoApi::class.java)
    }

    suspend fun getForecast(lat: Double, lon: Double): ForecastResponse {
        return api.getForecast(lat, lon)
    }

    suspend fun getClassement(
        lats: String, lons: String, dateStr: String
    ): List<Pair<Double?, Int?>> {
        val response = api.getMultiForecast(lats, lons)
        val results = mutableListOf<Pair<Double?, Int?>>()
        for (r in response) {
            if (r.daily == null) { results.add(null to null); continue }
            val dt = r.daily
            val idx = dt.time.indexOf(dateStr)
            if (idx >= 0 && dt.tempMax != null && idx < dt.tempMax.size) {
                results.add(dt.tempMax[idx] to (dt.weatherCode?.getOrNull(idx)))
            } else {
                results.add(null to null)
            }
        }
        return results
    }

    suspend fun searchCity(query: String): GeoResponse {
        return api.searchCity(query)
    }
}
