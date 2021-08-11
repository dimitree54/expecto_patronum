package we.rashchenko.networks.controllers

import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.Feedback

class ComplexController(private vararg val controllers: NeuralNetworkController) : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback> {
		val feedbacksPerController = controllers.map { it.getControllerFeedbacks(neurons, timeStep) }
		return neurons.indices.map { i ->
			Feedback(feedbacksPerController.sumOf { it[i].value } / controllers.size)
		}
	}
}