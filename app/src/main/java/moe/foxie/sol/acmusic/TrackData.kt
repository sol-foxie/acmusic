package moe.foxie.sol.acmusic


enum class Weather {
    SUNNY,RAINY,SNOWY
}
val SUNNY = Weather.SUNNY
val RAINY = Weather.RAINY
val SNOWY = Weather.SNOWY

val acnlTracks = hashMapOf<Pair<Int, Weather>,Int>(
    Pair(0,SUNNY) to R.raw.nl12am,
    Pair(1, SUNNY) to R.raw.nl2am,
    Pair(2, SUNNY) to R.raw.nl3am,
    Pair(3, SUNNY) to R.raw.nl4am,
    Pair(4, SUNNY) to R.raw.nl5am,
    Pair(5, SUNNY) to R.raw.nl6am,
    Pair(6, SUNNY) to R.raw.nl7am,
    Pair(7, SUNNY) to R.raw.nl8am,
    Pair(8, SUNNY) to R.raw.nl9am,
    Pair(9, SUNNY) to R.raw.nl10am,
    Pair(10, SUNNY) to R.raw.nl11am,
    Pair(11, SUNNY) to R.raw.nl12pm,
    Pair(12, SUNNY) to R.raw.nl1pm,
    Pair(13, SUNNY) to R.raw.nl2pm,
    Pair(14, SUNNY) to R.raw.nl3pm,
    Pair(15, SUNNY) to R.raw.nl4pm,
    Pair(16, SUNNY) to R.raw.nl5pm,
    Pair(17, SUNNY) to R.raw.nl6pm,
    Pair(18, SUNNY) to R.raw.nl7pm,
    Pair(19, SUNNY) to R.raw.nl8pm,
    Pair(20, SUNNY) to R.raw.nl9pm,
    Pair(21, SUNNY) to R.raw.nl10pm,
    Pair(22, SUNNY) to R.raw.nl11pm,
    Pair(23, SUNNY) to R.raw.nl1am
)
