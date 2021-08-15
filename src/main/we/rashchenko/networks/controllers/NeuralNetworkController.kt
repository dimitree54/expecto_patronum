package we.rashchenko.networks.controllers

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron

interface NeuralNetworkController {
	fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback>
}

