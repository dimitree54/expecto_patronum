package we.rashchenko.networks

import we.rashchenko.neurons.InputNeuron

interface NeuralNetworkWithInput : NeuralNetwork {
	val inputNeuronIDs: Collection<Int>
	fun addInputNeuron(neuron: InputNeuron): Int
}