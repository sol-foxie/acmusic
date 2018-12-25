package moe.foxie.sol.acmusic

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URL

class DarkSkyApi(private val key: String): WeatherManager.RemoteAPI(key) {

    private val parser = Parser()

    override val serviceName: String
        get() = "Dark Sky"

    override fun parseResponse(result: String): ACWeather {
        val obj = parser.parse(StringBuilder(result)) as JSONObject
        return when (obj.getJSONObject("hourly").getString("icon")) {
            "rain" -> ACWeather.RAINY
            "snow","sleet" -> ACWeather.SNOWY
            else -> ACWeather.SUNNY
        }
    }

    override fun constructRequest(location: LatLong): URL {
        return URL("https://api.darksky.net/forecast/${this.key}/${location.first},${location.second}")
    }
}