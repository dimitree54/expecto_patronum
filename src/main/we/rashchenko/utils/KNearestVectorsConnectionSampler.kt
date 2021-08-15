package we.rashchenko.utils

import we.rashchenko.utils.collections.WorstN
import java.lang.Integer.min

/**
 * Class helper for [k] nearest neighbours search based on distance between [Vector2]
 */
class KNearestVectorsConnectionSampler(private val k: Int) {
	/**
	 * Find [k] neighbours for each [Vector2] in [allPositions].
	 * @return map where keys from [allPositions] and values as collection of [k] neighbours. For small
	 *   [allPositions] with size < k+1 there maybe less than k neighbours in returned values.
	 */
	fun connectAll(allPositions: Collection<Vector2>): Map<Vector2, Collection<Vector2>> {
		val allPositionsList = allPositions.toList()
		val distances = Array(allPositions.size) { FloatArray(allPositions.size) }

		val result = mutableMapOf<Vector2, Collection<Vector2>>()
		for (i in allPositions.indices) {
			val kNearestNeighbourIndices = WorstN<Int>(k) { j1, j2 -> distances[i][j1].compareTo(distances[i][j2]) }
			kNearestNeighbourIndices.addAll(0 until i)

			for (j in i + 1 until allPositions.size) {
				allPositionsList[i].dst(allPositionsList[j]).also {
					distances[i][j] = it
					distances[j][i] = it
				}
				kNearestNeighbourIndices.add(j)
			}
			result[allPositionsList[i]] = allPositionsList.slice(kNearestNeighbourIndices)
		}

		return result
	}

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
