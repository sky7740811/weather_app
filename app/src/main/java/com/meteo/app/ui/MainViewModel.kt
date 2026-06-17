package com.meteo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meteo.app.data.model.City
import com.meteo.app.data.model.ForecastResponse
import com.meteo.app.data.model.GeoFeature
import com.meteo.app.data.repository.MeteoRepository
import com.meteo.app.data.repository.Preferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repo = MeteoRepository()
    val prefs = Preferences(application)

    // Current city
    private val _city = MutableStateFlow(City.DEFAULT)
    val city: StateFlow<City> = _city

    // Forecast data
    private val _forecast = MutableStateFlow<ForecastResponse?>(null)
    val forecast: StateFlow<ForecastResponse?> = _forecast

    private val _canRefresh = MutableStateFlow(true)
    val canRefresh: StateFlow<Boolean> = _canRefresh

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _classement = MutableStateFlow<List<Pair<Double?, Int?>>>(emptyList())
    val classement: StateFlow<List<Pair<Double?, Int?>>> = _classement

    private val _classLoading = MutableStateFlow(false)
    val classLoading: StateFlow<Boolean> = _classLoading

    private val _isFav = MutableStateFlow(false)
    val isFav: StateFlow<Boolean> = _isFav

    private val _tab = MutableStateFlow(0)
    val tab: StateFlow<Int> = _tab

    private val _suggestions = MutableStateFlow<List<GeoFeature>>(emptyList())
    val suggestions: StateFlow<List<GeoFeature>> = _suggestions

    private var searchJob: Job? = null

    private val _searchMode = MutableStateFlow(false)
    val searchMode: StateFlow<Boolean> = _searchMode

    private val _lastUpdate = MutableStateFlow<String?>(null)
    val lastUpdate: StateFlow<String?> = _lastUpdate

    fun setSearchMode(on: Boolean) { _searchMode.value = on }

    init {
        val cache = prefs.loadForecastCache()
        if (cache != null) {
            _city.value = cache.first
            _forecast.value = cache.second
        }
        loadForecast()
    }

    fun setTab(t: Int) { _tab.value = t }

    fun selectCity(city: City) {
        _city.value = city
        _isFav.value = prefs.isFav(city.name)
        prefs.addHist(city)
        loadForecast()
    }

    fun loadForecast() {
        if (!_canRefresh.value) return
        val cache = prefs.loadForecastCache()
        val now = System.currentTimeMillis()
        if (cache != null) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = cache.third
            val cacheHour = cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.DAY_OF_YEAR)
            cal.timeInMillis = now
            val nowHour = cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.DAY_OF_YEAR)
            if (cacheHour == nowHour) {
                _forecast.value = cache.second
                val d = Calendar.getInstance().apply { timeInMillis = cache.third }
                _lastUpdate.value = String.format("%02d/%02d %02d:%02d", d.get(Calendar.DAY_OF_MONTH), d.get(Calendar.MONTH)+1, d.get(Calendar.HOUR_OF_DAY), d.get(Calendar.MINUTE))
                return
            }
        }
        _canRefresh.value = false
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = repo.getForecast(_city.value.lat, _city.value.lon)
                _forecast.value = data
                prefs.saveForecastCache(_city.value, data)
                val now2 = Calendar.getInstance()
                _lastUpdate.value = String.format("%02d/%02d %02d:%02d", now2.get(Calendar.DAY_OF_MONTH), now2.get(Calendar.MONTH)+1, now2.get(Calendar.HOUR_OF_DAY), now2.get(Calendar.MINUTE))
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur de chargement"
            }
            _loading.value = false
        }
        viewModelScope.launch {
            kotlinx.coroutines.delay(30 * 60 * 1000L)
            _canRefresh.value = true
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

    fun searchSuggestions(query: String) {
        searchJob?.cancel()
        if (query.length < 2) { _suggestions.value = emptyList(); return }
        searchJob = viewModelScope.launch {
            delay(300)
            try {
                val res = repo.searchCity(query)
                _suggestions.value = res.features?.take(8) ?: emptyList()
            } catch (_: Exception) { _suggestions.value = emptyList() }
        }
    }

    fun selectSuggestion(f: GeoFeature) {
        _suggestions.value = emptyList()
        val p = f.properties
        val coords = f.geometry.coordinates
        val ctx = p.context?.split(",")?.getOrNull(1)?.trim() ?: ""
        selectCity(City(p.name, coords[1], coords[0], p.postcode ?: "", ctx))
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            try {
                val res = repo.searchCity(query)
                val f = res.features?.firstOrNull() ?: return@launch
                selectSuggestion(f)
            } catch (_: Exception) { }
        }
    }
}
