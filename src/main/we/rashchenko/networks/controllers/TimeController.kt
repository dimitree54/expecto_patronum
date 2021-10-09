package we.rashchenko.networks.controllers

import org.apache.commons.math3.stat.StatUtils
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.ZERO_DIV_EPS
import we.rashchenko.utils.clip
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * [NeuralNetworkController] that controls average time of different [ControlledNeuron] functions penalizing neurons
 *  that slower than average and encouraging ones that faster.
 * Note that new neurons that have their functions never called yet will have neutral feedback.
 * Then with more stats about functions time collected their Feedback will gradually move to real
 *  values discriminating slow and fast neurons. More about that you can check
 *  at [issue](https://github.com/dimitree54/ChNN-Library/issues/13)
 */
class TimeController : NeuralNetworkController {
	private fun getWeightByNumSteps(numControlledSteps: Int): Double{
		return tanh(numControlledSteps.toDouble() / 10) // after 30 steps it is almost one
	}

	private fun getWeightedFeedback(baseFeedback: Feedback, numControlledSteps: Int): Feedback{
		val w = getWeightByNumSteps(numControlledSteps)
		return Feedback(baseFeedback.value * w)
	}

	private fun getTimeFeedbacks(
		neurons: List<ControlledNeuron>,
		getTime: (ControlledNeuron)->Double,
		getNumSteps: (ControlledNeuron)->Int
	): List<Feedback> {
		neurons.map { getTime(it) }.toDoubleArray().let { times ->
			val meanTime = StatUtils.mean(times)
			val stdTime = sqrt(StatUtils.variance(times))
			return times.mapIndexed { i, time ->
				val baseFeedback = Feedback(-((time - meanTime) / (stdTime + ZERO_DIV_EPS)).clip(-1.0, 1.0))
				getWeightedFeedback(baseFeedback, getNumSteps(neurons[i]))
			}
		}
	}

	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>): List<Feedback> {
		val getActiveTimeFeedbacks = getTimeFeedbacks(
			neurons, ControlledNeuron::averageGetActiveTime, ControlledNeuron::numControlledGetActive)
		val updateTimeFeedbacks = getTimeFeedbacks(
			neurons, ControlledNeuron::averageUpdateTime, ControlledNeuron::numControlledUpdate)
		val forgetTimeFeedbacks = getTimeFeedbacks(
			neurons, ControlledNeuron::averageForgetTime, ControlledNeuron::numControlledForget)
		val getFeedbackTimeFeedbacks = getTimeFeedbacks(
			neurons, ControlledNeuron::averageGetFeedbackTime, ControlledNeuron::numControlledGetFeedback)
		val touchTimeFeedbacks = getTimeFeedbacks(
			neurons, ControlledNeuron::averageTouchTime, ControlledNeuron::numControlledTouch)
		return getActiveTimeFeedbacks.indices.map{ i->
			Feedback((getActiveTimeFeedbacks[i].value +
					updateTimeFeedbacks[i].value + forgetTimeFeedbacks[i].value +
					getFeedbackTimeFeedbacks[i].value + touchTimeFeedbacks[i].value) / 5)
		}
	}
}