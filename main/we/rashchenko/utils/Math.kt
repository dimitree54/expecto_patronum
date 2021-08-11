package we.rashchenko.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun Double.clip(minValue: Double = 0.0, maxValue: Double = 1.0): Double {
	return max(minValue, min(maxValue, this))
}

fun softmax(values: List<Double>): List<Double> {
	val e = values.map { Math.E.pow(it) }
	val eSum = e.sum()
	return e.map { it / eSum }
}