package we.rashchenko.utils

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Class for representing coordinate in 2D
 */
data class Vector2(val x: Float, val y: Float) {
	/**
	 * distance from this [Vector2] to [v]
	 */
	fun dst(v: Vector2): Float = sqrt((x - v.x).pow(2) + (y - v.y).pow(2))

	/**
	 * Scalar product with other [Vector2] [v] (coordinate-wise)
	 */
	fun scl(v: Vector2): Vector2 = Vector2(x * v.x, y * v.y)

	/**
	 * Scalar product
	 */
	fun scl(k: Float): Vector2 = Vector2(x * k, y * k)

	/**
	 * Length of the radius-vector represented by this vector
	 */
	fun len(): Float = dst(ZERO)

	/**
	 * Get the vector with the same direction, but unit length
	 */
	fun normalize(): Vector2 = len().let {
		if (it == 0f) {
			throw IllegalArgumentException()
		}
		scl(1 / it)
	}

	companion object {
		val ZERO = Vector2(0f, 0f)
		val ONES = Vector2(1f, 1f)
	}
}
