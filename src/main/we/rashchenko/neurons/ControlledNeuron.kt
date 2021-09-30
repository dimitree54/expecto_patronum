package we.rashchenko.neurons

import we.rashchenko.base.Feedback
import we.rashchenko.networks.controllers.NeuralNetworkController
import we.rashchenko.utils.ExponentialMovingAverage
import we.rashchenko.utils.ZERO_DIV_EPS
import kotlin.system.measureTimeMillis

/**
 * Wrapper for any other [baseNeuron] that reveals some of its statistics,
 *  for example [getAverageTime] or [getAverageActivity].
 * That statistics can be used by [NeuralNetworkController] to calculate external [Feedback] for that [Neuron]
 */
class ControlledNeuron(private val baseNeuron: Neuron) : Neuron by baseNeuron {
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
	private var numControlledTimeSteps = 0
	override fun update(feedback: Feedback, timeStep: Long) {
		if (control) {
			if (active) {
				numControlledActivations++
			}
			numControlledTimeSteps++
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
	override fun touch(sourceId: Int, timeStep: Long) {
		if (control) {
			measureTimeMillis {
				baseNeuron.touch(sourceId, timeStep)
			}.let { averageTouchTime.update(it.toDouble()) }
		} else {
			baseNeuron.touch(sourceId, timeStep)
		}
	}

	/**
	 * Get average runtime of that [Neuron]. That time includes all times of [Neuron] public functions.
	 * Note that time measured and averaged only for [control]led time steps.
	 * So, rarely called [Neuron] functions such as forgetSource are even more rarely estimated leaving it with
	 *  default value 0 as average time for that function.
	 * That may introduce some bias that neurons that have called rare functions will be considered as slower neurons
	 *  that others.
	 * Also, that may introduce bias against older neurons compared with new one that never estimated rare functions
	 *  time yet.
	 * To avoid the first bias try to make more steps [control]led.
	 * For the second one you can use [issue #13](https://github.com/dimitree54/ChNN-Library/issues/13) to discuss how
	 *  to solve that bias.
	 */
	fun getAverageTime(): Double = averageGetActiveTime.value + averageUpdateTime.value +
			averageForgetTime.value + averageFeedbackTime.value + averageTouchTime.value

	/**
	 * Get the rate of how often [baseNeuron] was active across all [control]led ticks.
	 */
	fun getAverageActivity(): Double {
		return numControlledActivations.toDouble() / (numControlledTimeSteps + ZERO_DIV_EPS)
	}
}