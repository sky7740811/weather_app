package com.meteo.app.data.model

data class City(
    val name: String,
    val lat: Double,
    val lon: Double,
    val postcode: String = "",
    val dept: String = ""
) {
    companion object {
        val DEFAULT = City("Fontaines-sur-Saône", 45.83572, 4.8449, "69270", "Rhône")
        val RANK_CITIES = listOf(
            City("Lyon", 45.76, 4.84, "", "Rhône"),
            City("Chambéry", 45.57, 5.92, "", "Savoie"),
            City("Annecy", 45.90, 6.13, "", "Haute-Savoie"),
            City("Grenoble", 45.18, 5.72, "", "Isère"),
            City("Valence", 44.93, 4.89, "", "Drôme"),
            City("Clermont-Ferrand", 45.78, 3.08, "", "Puy-de-Dôme"),
            City("Montélimar", 44.56, 4.75, "", "Drôme"),
            City("Le Puy-en-Velay", 45.04, 3.88, "", "Haute-Loire"),
            City("Gap", 44.56, 6.08, "", "Hautes-Alpes"),
            City("Bourg-Saint-Maurice", 45.62, 6.77, "", "Savoie"),
            City("Pontarlier", 46.90, 6.36, "", "Doubs"),
            City("Dijon", 47.32, 5.04, "", "Côte-d'Or"),
            City("Besançon", 47.24, 6.02, "", "Doubs"),
            City("Avignon", 43.95, 4.81, "", "Vaucluse"),
            City("Nîmes", 43.84, 4.36, "", "Gard"),
            City("Arles", 43.68, 4.63, "", "Bouches-du-Rhône")
        )
    }
}

data class HourData(
    val hour: Int,
    val temp: Int,
    val weatherCode: Int?,
    val uv: Double?,
    val rain: Int?,
    val cloud: Int?,
    val wind: Double?
)
