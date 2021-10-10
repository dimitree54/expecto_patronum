package we.rashchenko.base

/**
 * Wrapper for double feedback to make sure it meets limitations (to be in [[-1, 1]] range).
 * Feedback >0 is positive, 1 for high-quality neurons.
 * Feedback <0 is negative, -1 for neurons that does not help at all.
 * Feedback ==0 is neutral.
 */
data class Feedback(val value: Double) : Comparable<Feedback> {
	init {
		if (value !in -1.0..1.0) {
			throw IllegalArgumentException("Feedback should be in range [-1, 1]")
		}
	}

	companion object {
		val VERY_POSITIVE = Feedback(1.0)
		val VERY_NEGATIVE = Feedback(-1.0)
		val NEUTRAL = Feedback(0.0)
	}

	override fun compareTo(other: Feedback): Int {
		return value.compareTo(other.value)
	}
}
