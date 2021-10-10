package we.rashchenko.utils

import java.util.*

/**
 * Infinite sequence of random integers without repetition. In order to avoid repetitions it stores all
 *  previously sampled ids, so that sampler can become bigger and slower with time, be careful.
 */
val randomIds = sequence {
    val existingIds = mutableSetOf<Int>()
    val random = Random()
    while (true) {
        val randomId = random.nextInt()
        if (randomId !in existingIds) {
            existingIds.add(randomId)
            yield(randomId)
        }
    }
}.iterator()