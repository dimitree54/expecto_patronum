package we.rashchenko.utils

// https://blog.fugue88.ws/archives/2017-01/The-correct-way-to-start-an-Exponential-Moving-Average-EMA
class ExponentialMovingAverageHotStart(private val eps: Double = 0.99) {
    private var _value: Double = 0.0  // Default value is meaningless, try to update at least once before first get.
    private var extra: Double = 1.0
    val value: Double
        get() {
            if (extra == 1.0) {
                return _value
            }
            return _value / (1 - extra)
        }

    fun update(newValue: Double) {
        extra *= eps
        _value = _value * eps + newValue * (1 - eps)
    }

    override fun toString(): String {
        return value.toString()
    }
}