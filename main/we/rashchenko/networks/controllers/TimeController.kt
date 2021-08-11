package we.rashchenko.networks.controllers

import org.apache.commons.math3.stat.StatUtils
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.Feedback
import we.rashchenko.utils.clip
import kotlin.math.sqrt

class TimeController : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback> {
		neurons.map { it.getAverageTime() }.toDoubleArray().let { times ->
			val mean = StatUtils.mean(times)
			val std = sqrt(StatUtils.variance(times))
			return times.map { Feedback(-((it - mean) / (std + 0.0001)).clip(-1.0, 1.0)) }
		}
	}
}