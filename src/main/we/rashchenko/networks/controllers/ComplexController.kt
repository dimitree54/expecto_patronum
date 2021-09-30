package we.rashchenko.networks.controllers

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron

/**
 * [NeuralNetworkController] that calls one or more other [controllers]
 *  and average their [getControllerFeedbacks] output.
 */
class ComplexController(private vararg val controllers: NeuralNetworkController) : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>): List<Feedback> {
		val feedbacksPerController = controllers.map { it.getControllerFeedbacks(neurons) }
		return neurons.indices.map { i ->
			Feedback(feedbacksPerController.sumOf { it[i].value } / controllers.size)
		}
	}
}