package moe.foxie.sol.acmusic

import java.lang.Exception

/**
 * an enum representing various weather conditions as they appear in the Animal Crossing games.
 */
enum class ACWeather {
    SUNNY,RAINY,SNOWY
}

/**
 * this class dispenses the weather.
 */
class WeatherManager {

    enum class Connectivity {
        ONLINE,OFFLINE
    }

    private class WeatherFetchFailureException(message: String, reason: WeatherFetchFailureException.Reason): Exception(message) {
        enum class Reason {
            /** location access is disallowed by system privacy settings. */
            NOLOCATION,
            /** device is unable to access the internet. */
            DEVICEOFFLINE,
            /** the app has been put into offline mode, this is a user preference. */
            OFFLINEMODE,
            /** the api was successfully connected to, but returned an error. */
            REMOTEAPIFAILURE,
        }
    }

    data class Forecast(val connection: Connectivity, val weather: ACWeather)

    /**
     * returns a Forecast.
     * iff location access is enabled AND the device is connected to the internet AND the api calls are successful,
     * the Forecast object returned will be representative of the weather in the user's current location.
     * otherwise the weather will be generated based on current season and randomness.
     */
    fun currentWeather(): Forecast {
        return try {
            Forecast(Connectivity.ONLINE, onlineWeather())
        } catch (e: WeatherFetchFailureException) {
            Forecast(Connectivity.OFFLINE, offlineWeather())
        }
    }

    /**
     * returns an ACWeather object chosen quasi-randomly.
     * in the months from november through february:
     * there is a 30% chance of SNOW, 10% chance of RAINY, and 60% chance of SUNNY.
     * at all other times the odds are:
     * 20% RAINY and 80% SUNNY.
     */
    private fun offlineWeather(): ACWeather {
        TODO()
    }

    /**
     * returns a ACWeather object based on the user's current location. this method may fail if:
     * we do not have location access, the device is disconnected, the app is in offline mode, or the api call fails.
     * in the case of the weather provider's api call failing, this method may try several other apis to see if any still work.
     * callers of this function are encouraged to catch the exception and call offlineWeather() in that event.
     * @throws WeatherFetchFailureException
     */
    private fun onlineWeather(): ACWeather {
        TODO()
    }

}