package we.rashchenko.utils

import java.lang.Integer.min

class KNearestVectorsConnectionSampler(private val k: Int) {
	fun connectAll(allPositions: Collection<Vector2>): Map<Vector2, Collection<Vector2>> {
		val allPositionsList = allPositions.toList()
		val distances = Array(allPositions.size) { FloatArray(allPositions.size) }

		val result = mutableMapOf<Vector2, Collection<Vector2>>()
		for (i in allPositions.indices) {
			val kNearestNeighbourIndices = BestN<Int>(k) { j1, j2 -> -distances[i][j1].compareTo(distances[i][j2]) }
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
	 * As we can not rewire existing connections from connections sampler, calling connectNew breaks
	 * kNearestStructure. That function connects newPosition to kNearest positions, but also connects each
	 * of those nearest positions to that new position, making them not kNearest.
	 */
	fun connectNew(
		newPosition: Vector2,
		allPositions: Collection<Vector2>
	): Map<Vector2, Collection<Vector2>> {
		val result = mutableMapOf<Vector2, Collection<Vector2>>()
		val closestPositions = allPositions.sortedBy { it.dst(newPosition) }
		if (closestPositions[0] != newPosition) throw IllegalArgumentException("newPosition not in allPositions")
		val fromConnections = closestPositions.slice(1..min(k, allPositions.size - 1))
		result[newPosition] = fromConnections
		fromConnections.forEach { result[it] = listOf(newPosition) }
		return result
	}
}
