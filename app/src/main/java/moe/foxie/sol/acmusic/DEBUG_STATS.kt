package moe.foxie.sol.acmusic

object DEBUG_STATS {
    var CURRENT_TRACK = ""
    var CURRENT_MODE: WeatherManager.Connectivity? = null
    var CURRENT_LATLONG: LatLong? = null
}

fun TRACE_LatLong(ll: LatLong): LatLong {
    DEBUG_STATS.CURRENT_LATLONG = ll
    return ll
}