package we.rashchenko.patronum.database.stats

import kotlin.math.abs

class GlobalStats {
    var sortedReputations: Array<Float> = arrayOf()
    fun getRateOnLeaderBoard(reputation: Float): Float{
        val place = sortedReputations.binarySearch(reputation)
        return abs(place).toFloat() / sortedReputations.size
    }
}