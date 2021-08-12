package we.rashchenko.networks.controllers

import org.apache.commons.math3.stat.StatUtils
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.Feedback
import we.rashchenko.utils.clip
import kotlin.math.abs
import kotlin.math.sqrt

class ActivityController : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback> {
		neurons.map { it.getAverageActivity(timeStep) }.toDoubleArray().let { activities ->
			val mean = StatUtils.mean(activities)
			val std = sqrt(StatUtils.variance(activities))
			// near average activity is good (~1.0), deviation in both sides bad (down to -1.0)
			return activities.map { Feedback((1 - 2 * abs(it - mean) / (std + 0.001)).clip(-1.0, 1.0)) }
		}
	}
}