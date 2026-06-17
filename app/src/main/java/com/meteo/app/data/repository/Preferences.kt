package com.meteo.app.data.repository

import android.content.Context
import com.meteo.app.data.model.City
import com.meteo.app.data.model.ForecastResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Preferences(context: Context) {
    private val prefs = context.getSharedPreferences("meteo", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Favorites
    fun getFavs(): MutableList<City> {
        val json = prefs.getString("favs", null) ?: return mutableListOf()
        return try {
            gson.fromJson(json, object : TypeToken<List<City>>() {}.type) ?: mutableListOf()
        } catch (e: Exception) { mutableListOf() }
    }

    fun saveFavs(list: List<City>) {
        prefs.edit().putString("favs", gson.toJson(list)).apply()
    }

    fun toggleFav(city: City): Boolean {
        val list = getFavs().toMutableList()
        val idx = list.indexOfFirst { it.name == city.name }
        return if (idx >= 0) { list.removeAt(idx); saveFavs(list); false }
        else { list.add(city); saveFavs(list); true }
    }

    fun isFav(name: String): Boolean = getFavs().any { it.name == name }

    // History
    fun getHist(): MutableList<City> {
        val json = prefs.getString("hist", null) ?: return mutableListOf()
        return try {
            gson.fromJson(json, object : TypeToken<List<City>>() {}.type) ?: mutableListOf()
        } catch (e: Exception) { mutableListOf() }
    }

    fun saveHist(list: List<City>) {
        prefs.edit().putString("hist", gson.toJson(list)).apply()
    }

    // Forecast cache
    fun saveForecastCache(city: City, forecast: ForecastResponse) {
        prefs.edit()
            .putString("cache_city", gson.toJson(city))
            .putString("cache_forecast", gson.toJson(forecast))
            .putLong("cache_time", System.currentTimeMillis())
            .apply()
    }

    fun loadForecastCache(): Triple<City, ForecastResponse, Long>? {
        val cityJson = prefs.getString("cache_city", null) ?: return null
        val forecastJson = prefs.getString("cache_forecast", null) ?: return null
        val cacheTime = prefs.getLong("cache_time", 0L)
        return try {
            val city = gson.fromJson(cityJson, City::class.java)
            val forecast = gson.fromJson(forecastJson, ForecastResponse::class.java)
            Triple(city, forecast, cacheTime)
        } catch (e: Exception) { null }
    }

    fun addHist(city: City) {
        val list = getHist().toMutableList()
        list.removeAll { it.name == city.name }
        list.add(0, city)
        if (list.size > 20) list.removeAt(list.lastIndex)
        prefs.edit().putString("hist", gson.toJson(list)).apply()
    }
}
