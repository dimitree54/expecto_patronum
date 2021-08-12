package we.rashchenko.networks

import we.rashchenko.neurons.InputNeuron
import we.rashchenko.neurons.Neuron
import we.rashchenko.utils.*

class StochasticNeuralNetwork : NeuralNetworkWithInput {
	private val neuronsWithID = mutableMapOf<Int, Neuron>()
	override val neuronIDs: Collection<Int>
		get() = neuronsWithID.keys
	private val inputNeuronsWithID = mutableMapOf<Int, InputNeuron>()
	override val inputNeuronIDs
		get() = inputNeuronsWithID.keys

	override val connections = mutableMapOf<Int, MutableList<Int>>()
	private val backwardConnections = mutableMapOf<Int, MutableList<Int>>()

	private val neuronFeedbacks = mutableMapOf<Int, ExponentialMovingAverage>()

	override fun add(neuron: Neuron): Int {
		val id = randomIds.next()
		neuronsWithID[id] = neuron
		connections[id] = mutableListOf()
		backwardConnections[id] = mutableListOf()
		neuronFeedbacks[id] = ExponentialMovingAverage(0.0, 0.9)
		return id
	}

	override fun addInputNeuron(neuron: InputNeuron): Int {
		return add(neuron).also { id ->
			inputNeuronsWithID[id] = neuron
		}
	}

	private fun removeConnections(neuronID: Int) {
		connections[neuronID]!!.forEach {
			backwardConnections[it]!!.remove(neuronID)
		}
		backwardConnections[neuronID]!!.forEach {
			connections[it]!!.remove(neuronID)
		}
		connections.remove(neuronID)
		backwardConnections.remove(neuronID)
	}

	override fun remove(neuronID: Int): Boolean {
		connections[neuronID]?.forEach { neuronsWithID[it]!!.forgetSource(neuronID) } ?: return false
		removeConnections(neuronID)
		neuronFeedbacks.remove(neuronID)
		nextTickNeurons.remove(neuronID)
		neuronsWithID.remove(neuronID)
		inputNeuronsWithID.remove(neuronID)
		return true
	}

	override fun addConnection(fromNeuronID: Int, toNeuronID: Int): Boolean {
		if (fromNeuronID !in neuronIDs || toNeuronID !in neuronIDs) {
			return false
		}
		connections[fromNeuronID]!!.add(toNeuronID)
		backwardConnections[toNeuronID]!!.add(fromNeuronID)
		return true
	}

	private var nextTickNeurons = mutableSetOf<Int>()
	private val setAddingLock = Object()
	override fun tick() {
		val currentTickNeurons = nextTickNeurons
		nextTickNeurons = mutableSetOf()
		currentTickNeurons.forEach { source ->
			connections[source]!!.forEach { receiver ->
				touch(source, receiver)
			}
		}
		currentTickNeurons.addAll(inputNeuronIDs)  // we update inputNeurons even if it is not active.
		currentTickNeurons.forEach { id ->
			neuronsWithID[id]!!.also { neuron ->
				neuron.update(getFeedback(id)!!, timeStep)
				if (neuron.active) {
					synchronized(setAddingLock) {
						nextTickNeurons.add(id)
					}
				}
			}
		}
		nextTickNeurons.addAll(inputNeuronIDs.filter { neuronsWithID[it]!!.active })
		timeStep++
	}

	override var timeStep: Long = 0
		private set

	override fun getFeedback(neuronID: Int): Feedback? {
		return inputNeuronsWithID[neuronID]?.getInternalFeedback() ?: neuronFeedbacks[neuronID]?.getFeedback()
	}

	override fun getNeuron(neuronID: Int): Neuron? = neuronsWithID[neuronID]

	private fun touch(sourceID: Int, receiverID: Int) {
		val receiver = neuronsWithID[receiverID]!!
		synchronized(receiver) {
			val isReceiverInput = receiverID in inputNeuronIDs
			if (receiverID !in nextTickNeurons || isReceiverInput) {
				if (receiver.touch(sourceID, timeStep) || isReceiverInput) {
					val feedbackUpdate = receiver.getFeedback(sourceID)
					synchronized(setAddingLock) {
						neuronFeedbacks[sourceID]!!.update(feedbackUpdate)
						nextTickNeurons.add(receiverID)
					}
				}
			}
		}
	}
}