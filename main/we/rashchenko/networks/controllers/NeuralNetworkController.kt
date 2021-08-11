package we.rashchenko.networks.controllers

import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.Feedback

interface NeuralNetworkController {
	fun getControllerFeedbacks(neurons: List<ControlledNeuron>, timeStep: Long): List<Feedback>
}

