package we.rashchenko.utils

import java.lang.Integer.min

/**
 * Class helper for [k] nearest neighbours search based on distance between [Vector2]
 */
class KNearestVectorsConnectionSampler(private val k: Int) {
    /**
     * Calculates [k] nearest to the [newPosition] positions from [allPositions]
     * @param newPosition a new position, it should be in [allPositions]
     * @param allPositions all the other positions, should contain [newPosition]
     * @return Collection with [k] nearest neighbours of [newPosition]. For small
     *   [allPositions] with size < k+1 there maybe less than k neighbours in returned collection.
     */
    fun connectNew(
        newPosition: Vector2,
        allPositions: Collection<Vector2>
    ): Collection<Vector2> {
        val closestPositions = allPositions.sortedBy { it.dst(newPosition) }
        if (closestPositions[0] != newPosition) throw IllegalArgumentException("newPosition not in allPositions")
        return closestPositions.slice(1..min(k, allPositions.size - 1))
    }
}
