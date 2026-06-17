package com.meteo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meteo.app.data.model.City
import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.data.repository.MeteoRepository
import com.meteo.app.data.repository.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repo = MeteoRepository()
    val prefs = Preferences(application)

    // Current city
    private val _city = MutableStateFlow(City.DEFAULT)
    val city: StateFlow<City> = _city

    // Forecast data
    private val _forecast = MutableStateFlow<ForecastResponse?>(null)
    val forecast: StateFlow<ForecastResponse?> = _forecast

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Classement
    private val _classement = MutableStateFlow<List<Pair<Double?, Int?>>>(emptyList())
    val classement: StateFlow<List<Pair<Double?, Int?>>> = _classement

    private val _classLoading = MutableStateFlow(false)
    val classLoading: StateFlow<Boolean> = _classLoading

    // Fav button state
    private val _isFav = MutableStateFlow(false)
    val isFav: StateFlow<Boolean> = _isFav

    // Selected tab
    private val _tab = MutableStateFlow(0)
    val tab: StateFlow<Int> = _tab

    fun setTab(t: Int) { _tab.value = t }

    fun selectCity(city: City) {
        _city.value = city
        _isFav.value = prefs.isFav(city.name)
        prefs.addHist(city)
        loadForecast()
    }

    fun loadForecast() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = repo.getForecast(_city.value.lat, _city.value.lon)
                _forecast.value = data
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur de chargement"
            }
            _loading.value = false
        }
    }

    fun loadClassement(dateStr: String) {
        viewModelScope.launch {
            _classLoading.value = true
            try {
                val lats = City.RANK_CITIES.joinToString(",") { it.lat.toString() }
                val lons = City.RANK_CITIES.joinToString(",") { it.lon.toString() }
                val data = repo.getClassement(lats, lons, dateStr)
                _classement.value = data
            } catch (e: Exception) { }
            _classLoading.value = false
        }
    }

    fun toggleFav() {
        val added = prefs.toggleFav(_city.value)
        _isFav.value = added
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            try {
                val res = repo.searchCity(query)
                val f = res.features?.firstOrNull() ?: return@launch
                val p = f.properties
                val coords = f.geometry.coordinates
                val ctx = p.context?.split(",")?.getOrNull(1)?.trim() ?: ""
                selectCity(City(p.name, coords[1], coords[0], p.postcode ?: "", ctx))
            } catch (_: Exception) { }
        }
    }
}
