package moe.foxie.sol.acmusic

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.io.*
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.random.Random

/**
 * an enum representing various weather conditions as they appear in the Animal Crossing games.
 */
enum class ACWeather {
    SUNNY,RAINY,SNOWY
}
/**
 * a structure suitable for representing a location as a latitude and longitude.
 */
typealias LatLong = Pair<Double,Double>

/**
 * this class dispenses the weather.
 */
class WeatherManager(val onlineMode: Boolean, private val context: Context, private val apis: List<RemoteAPI>) {

    private val locationProvider: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    enum class Connectivity {
        ONLINE,OFFLINE
    }

    private abstract class WeatherFetchFailureException(message: String): Exception(message)
    /** location access is disallowed by system privacy settings. */
    private class WeatherFetchNoLocationAccessException(): WeatherFetchFailureException("access to the user's location was denied.")
    /** device was unable to provide location */
    private class WeatherFetchNoLocationException(): WeatherFetchFailureException("access to the user's location was denied.")
    /** device is unable to access the internet. */
    private class WeatherFetchNoNetworkException(): WeatherFetchFailureException("the device is not connected to the network.")
    /** the app has been put into offline mode, this is a user preference. */
    private class WeatherFetchOfflineModeException(): WeatherFetchFailureException("attempted to fetch from network while in offline-only mode.")
    /** the api was successfully connected to, but returned an error. */
    private class WeatherFetchRemoteAPIFailureException(message: String, errorCode: Int): WeatherFetchFailureException(message)

    data class Forecast(val connection: Connectivity, val weather: ACWeather)

    /**
     * returns a Forecast.
     * iff location access is enabled AND the device is connected to the internet AND the api calls are successful,
     * the Forecast object returned will be representative of the weather in the user's current location.
     * otherwise the weather will be generated based on current season and randomness.
     */
    fun currentWeather(): Forecast {
        return try {
            Forecast(Connectivity.ONLINE, onlineWeather()) //network request that can throw
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
        return when (Calendar.getInstance().get(Calendar.MONTH)) {
            NOVEMBER, DECEMBER, JANUARY, FEBRUARY -> determineWeather(10,30)
            else -> determineWeather(20,0)
        }
    }

    private fun determineWeather(rainyOdds: Int, snowyOdds: Int): ACWeather =
        chooseWithProbability(100, Pair(RAINY,rainyOdds),Pair(SNOWY,snowyOdds),Pair(SUNNY,100 - (rainyOdds + snowyOdds)))


    /**
     * returns a ACWeather object based on the user's current location. this method may fail if:
     * we do not have location access, the device is disconnected, the app is in offline mode, or the api call fails.
     * in the case of the weather provider's api call failing, this method may try several other apis to see if any still work.
     * callers of this function are encouraged to catch the exception and call offlineWeather() in that event.
     * @throws WeatherFetchFailureException
     */
    private fun onlineWeather(): ACWeather {
        if (!onlineMode) throw WeatherFetchOfflineModeException()
        return tryApis(this.apis,getCurrentLocation())

    }

    /**
     * takes a list of RemoteAPI instances and fetches from them, trying each one in the list
     * in case of failure.
     * @return an ACWeather from the first RemoteAPI instance that successfully returns
     * @throws WeatherFetchNoNetworkException if at any time the network is not available this is thrown.
     * @throws WeatherFetchRemoteAPIFailureException if all apis fail the exception generated by the last instance in the list will be thrown.
     */
    private fun tryApis(apis: List<RemoteAPI>,location: LatLong): ACWeather {
        require(apis.isNotEmpty())
        if (!context.getConnectivityManager().isConnected()) throw WeatherFetchNoNetworkException()

        return try {
            apis.component1().fetch(location)
        } catch (e: WeatherFetchFailureException) {
            val rest = apis.drop(1)
            if (rest.isNotEmpty()) tryApis(rest,location) else throw e
        }
    }

    /**
     * gets the current location
     * @returns the user's latitude and longitude position
     * @throws WeatherFetchNoLocationAccessException
     * @throws WeatherFetchNoLocationException
     */
    private fun getCurrentLocation(): LatLong {
        try {
            val location = Tasks.await(locationProvider.lastLocation)
            if (location != null) return LatLong(location.latitude, location.longitude)
            throw WeatherFetchNoLocationException()
        } catch (e: SecurityException) {
            throw WeatherFetchNoLocationAccessException()
        }
    }

    abstract class RemoteAPI(private val key: String) {

        abstract val serviceName: String

        var done = false
            private set

        sealed class Result {
            data class Success(val rawResult: String, val parsedResult: ACWeather) : Result()
            data class Failure(val error: IOException) : Result()
        }

        var result: Result? = null
            private set

        /**
         * attempts to fetch the weather from the api
         * @throws WeatherFetchRemoteAPIFailureException
         */
        fun fetch(location: LatLong): Result {
            done = false
            val connection = constructRequest(location).openConnection() as HttpsURLConnection
            try {
                val rawResult = connection.inputStream.use { readStream(it, connection.contentLength) }
                val success = Result.Success(rawResult, parseResponse(rawResult))
                result = success
                return success
            } catch(e: IOException) {
                val failure = Result.Failure(e)
                result = failure
                return failure
            } finally {
                connection.disconnect()
            }
            done = true
        }
        protected abstract fun parseResponse(result: String): ACWeather

        protected abstract fun constructRequest(location: LatLong): URL
    }
}

fun UNREACHABLE(): Nothing = throw IllegalStateException()

fun <T> chooseWithProbability(total: Int, vararg odds: Pair<T,Int>): T {
    require(total > 0 && odds.all { (_,odd) ->  odd > 0})
    require(odds.fold(0) { n, (_,odd) -> n + odd } == total)

    val roll = Random.nextInt(total)

    var sum = 0
    for ((value,odd) in odds) {
        sum += odd
        if (roll < sum) return value
    }
    UNREACHABLE()
}

fun Context.getConnectivityManager(): ConnectivityManager {
    return this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
fun ConnectivityManager.isConnected(): Boolean {
    return this.activeNetworkInfo.isConnectedOrConnecting
}

/**
 * Converts the contents of an InputStream to a String.
 * copied on November 4th, 2018 from https://developer.android.com/training/basics/network-ops/connecting
 */
@Throws(IOException::class, UnsupportedEncodingException::class)
fun readStream(stream: InputStream, maxReadSize: Int): String? {
    val reader: Reader? = InputStreamReader(stream, "UTF-8")
    val rawBuffer = CharArray(maxReadSize)
    val buffer = StringBuffer()
    var readSize: Int = reader?.read(rawBuffer) ?: -1
    var maxReadBytes = maxReadSize
    while (readSize != -1 && maxReadBytes > 0) {
        if (readSize > maxReadBytes) {
            readSize = maxReadBytes
        }
        buffer.append(rawBuffer, 0, readSize)
        maxReadBytes -= readSize
        readSize = reader?.read(rawBuffer) ?: -1
    }
    return buffer.toString()
}