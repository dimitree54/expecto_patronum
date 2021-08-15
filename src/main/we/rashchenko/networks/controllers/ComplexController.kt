package we.rashchenko.networks.controllers

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron

class ComplexController(private vararg val controllers: NeuralNetworkController) : NeuralNetworkController {
	override fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback> {
		val feedbacksPerController = controllers.map { it.getControllerFeedbacks(neurons, timeStep) }
		return neurons.indices.map { i ->
			Feedback(feedbacksPerController.sumOf { it[i].value } / controllers.size)
		}
	}
}