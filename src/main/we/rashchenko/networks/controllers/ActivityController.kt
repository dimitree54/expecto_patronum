package we.rashchenko.networks.controllers

import org.apache.commons.math3.stat.StatUtils
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.ZERO_DIV_EPS
import we.rashchenko.utils.clip
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * [NeuralNetworkController] that controls neuron average activity to be close to the average.
 * Assigns penalty for neurons that far from the average and encourage ones that close to the average.
 */
class ActivityController : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>): List<Feedback> {
		neurons.map { it.getAverageActivity() }.toDoubleArray().let { activities ->
			val mean = StatUtils.mean(activities)
			val std = sqrt(StatUtils.variance(activities))
			// near average activity is good (~1.0), deviation in both sides bad (down to -1.0)
			return activities.map { Feedback(1 - 2 * (abs(it - mean) / (std + ZERO_DIV_EPS)).clip(-1.0, 1.0)) }
		}
	}
}