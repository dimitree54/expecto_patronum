package we.rashchenko.neurons

import we.rashchenko.utils.ExponentialMovingAverage
import we.rashchenko.utils.Feedback
import kotlin.system.measureTimeMillis

class ControlledNeuron(private val baseNeuron: Neuron, private val creationTimeStep: Long) : Neuron by baseNeuron {
	var control: Boolean = false

	private val averageGetActiveTime = ExponentialMovingAverage(0.0)
	override val active: Boolean
		get() {
			return if (control) {
				val result: Boolean
				measureTimeMillis {
					result = baseNeuron.active
				}.let { averageGetActiveTime.update(it.toDouble()) }
				result
			} else {
				baseNeuron.active
			}
		}

	private val averageUpdateTime = ExponentialMovingAverage(0.0)
	private var numControlledActivations = 0
	override fun update(feedback: Feedback, timeStep: Long) {
		if (control) {
			if (active) {
				numControlledActivations++
			}
			measureTimeMillis {
				baseNeuron.update(feedback, timeStep)
			}.let { averageUpdateTime.update(it.toDouble()) }
		} else {
			baseNeuron.update(feedback, timeStep)
		}
	}

	private val averageForgetTime = ExponentialMovingAverage(0.0)
	override fun forgetSource(sourceId: Int) {
		if (control) {
			measureTimeMillis {
				baseNeuron.forgetSource(sourceId)
			}.let { averageForgetTime.update(it.toDouble()) }
		} else {
			baseNeuron.forgetSource(sourceId)
		}
	}

	private val averageFeedbackTime = ExponentialMovingAverage(0.0)
	override fun getFeedback(sourceId: Int): Feedback {
		return if (control) {
			val result: Feedback
			measureTimeMillis {
				result = baseNeuron.getFeedback(sourceId)
			}.let { averageFeedbackTime.update(it.toDouble()) }
			result
		} else {
			baseNeuron.getFeedback(sourceId)
		}
	}

	private val averageTouchTime = ExponentialMovingAverage(0.0)
	override fun touch(sourceId: Int, timeStep: Long): Boolean {
		return if (control) {
			val result: Boolean
			measureTimeMillis {
				result = baseNeuron.touch(sourceId, timeStep)
			}.let { averageTouchTime.update(it.toDouble()) }
			result
		} else {
			baseNeuron.touch(sourceId, timeStep)
		}
	}

	fun getAverageTime(): Double = averageGetActiveTime.value + averageUpdateTime.value +
			averageForgetTime.value + averageFeedbackTime.value + averageTouchTime.value

	fun getAverageActivity(timeStep: Long): Double {
		return numControlledActivations.toDouble() / (timeStep - creationTimeStep + 1)
	}
}