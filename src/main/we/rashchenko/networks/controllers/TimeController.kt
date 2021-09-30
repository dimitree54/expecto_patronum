package we.rashchenko.networks.controllers

import org.apache.commons.math3.stat.StatUtils
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.ZERO_DIV_EPS
import we.rashchenko.utils.clip
import kotlin.math.sqrt

/**
 * [NeuralNetworkController] that controls [ControlledNeuron.averageFeedbackTime] penalizing neurons that slower than
 *  average and encouraging ones that faster.
 * Note that there is a [problem](https://github.com/dimitree54/ChNN-Library/issues/13) that adds bias against
 *  old neurons compared to newly created.
 */
class TimeController : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>): List<Feedback> {
		neurons.map { it.getAverageTime() }.toDoubleArray().let { times ->
			val mean = StatUtils.mean(times)
			val std = sqrt(StatUtils.variance(times))
			return times.map { Feedback(-((it - mean) / (std + ZERO_DIV_EPS)).clip(-1.0, 1.0)) }
		}
	}
}