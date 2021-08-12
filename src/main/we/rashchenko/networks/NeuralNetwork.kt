package we.rashchenko.networks

import we.rashchenko.base.Ticking
import we.rashchenko.neurons.Neuron
import we.rashchenko.utils.Feedback

interface NeuralNetwork : Ticking {
	val neuronIDs: Collection<Int>
	val connections: Map<Int, Collection<Int>>

	fun add(neuron: Neuron): Int
	fun remove(neuronID: Int): Boolean
	fun addConnection(fromNeuronID: Int, toNeuronID: Int): Boolean
	fun getFeedback(neuronID: Int): Feedback?
	fun getNeuron(neuronID: Int): Neuron?
}

