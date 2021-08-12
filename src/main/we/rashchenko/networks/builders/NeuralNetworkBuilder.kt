package we.rashchenko.networks.builders

import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.utils.Feedback

interface NeuralNetworkBuilder {
	val neuralNetwork: NeuralNetwork
	fun addNeuron(): Int
	fun remove(neuronID: Int): Boolean
	fun reportFeedback(neuronID: Int, feedback: Feedback)
}