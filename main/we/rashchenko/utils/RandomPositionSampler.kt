package we.rashchenko.utils

import java.util.*

/**
 * Samples Vector2 positions with x and y in 0..1 range
 * Rectangular sampler samples positions with different probabilities for x and y. So if you need uniformly fill
 *  rectangular box with point use it, density for x and y will be equal in that box.
 */
class RandomPositionSampler : Iterator<Vector2> {
	private val random = Random()
	override fun next(): Vector2 = Vector2(random.nextFloat(), random.nextFloat())
	override fun hasNext(): Boolean = true
}