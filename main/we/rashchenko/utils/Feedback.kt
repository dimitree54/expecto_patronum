package we.rashchenko.utils

/**
 * Wrapper for double feedback to make sure it meets limitations.
 * feedback > 0 is positive, for high-quality neurons;
 * feedback < 0 is negative, for neurons that does not help;
 * feedback == 0 is neutral
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

fun ExponentialMovingAverage.update(feedback: Feedback) {
	return this.update(feedback.value)
}

fun ExponentialMovingAverage.getFeedback(): Feedback {
	return Feedback(value)
}

