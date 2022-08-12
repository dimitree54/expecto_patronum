package we.rashchenko.patronum.database.stats

import kotlin.math.abs

class GlobalStats {
    var sortedReputations: Array<Int> = arrayOf()
    fun getRateOnLeaderBoard(reputation: Int): Float{
        val place = sortedReputations.binarySearch(reputation)
        return (abs(place) + 1).toFloat() / sortedReputations.size
    }
}