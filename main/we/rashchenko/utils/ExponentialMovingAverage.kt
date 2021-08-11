package we.rashchenko.utils

class ExponentialMovingAverage(initialValue: Double, private val eps: Double = 0.99) {
	var value: Double = initialValue
		private set

	fun update(newValue: Double) {
		value = value * eps + newValue * (1 - eps)
	}

	override fun toString(): String {
		return value.toString()
	}
}