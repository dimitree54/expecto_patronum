package we.rashchenko.utils

import java.util.*

/**
 * Samples Vector2 positions with x and y in 0..1 range
 */
class RandomPositionSampler : Iterator<Vector2> {
    private val random = Random()
    override fun next(): Vector2 = Vector2(random.nextFloat(), random.nextFloat())
    override fun hasNext(): Boolean = true
}