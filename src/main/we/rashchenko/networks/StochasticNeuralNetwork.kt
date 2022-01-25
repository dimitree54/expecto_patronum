package we.rashchenko.networks

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.inputs.InputNeuron
import we.rashchenko.utils.ExponentialMovingAverage
import we.rashchenko.utils.IDsGenerator

/**
 * Main implementation of the [NeuralNetworkWithInput].
 * It is [NeuralNetwork] optimised to work with sparse activation by skipping some [Neuron] touches.
 * It is called stochastic because it may change behaviour based on the order of [Neuron] calls.
 * Note that [StochasticNeuralNetwork] is used for ChNN contest and that [Neuron] documentation considers that.
 */
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
    private val ids = IDsGenerator()

    override fun add(neuron: Neuron): Int {
        val id = ids.next()
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
        if (connections[fromNeuronID]!!.contains(toNeuronID)) {
            throw Exception("Adding the same connection again. Check whether your builder correct.")
        }
        connections[fromNeuronID]!!.add(toNeuronID)
        backwardConnections[toNeuronID]!!.add(fromNeuronID)
        return true
    }

    override fun removeConnection(fromNeuronID: Int, toNeuronID: Int): Boolean {
        val connectionExist = connections[fromNeuronID]?.contains(toNeuronID) ?: false
        return if (connectionExist) {
            neuronsWithID[toNeuronID]!!.forgetSource(fromNeuronID)
            connections[fromNeuronID]!!.remove(toNeuronID)
            backwardConnections[toNeuronID]!!.remove(fromNeuronID)
            true
        } else {
            false
        }
    }

    private var nextTickNeurons = mutableSetOf<Int>()
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
                    nextTickNeurons.add(id)
                }
            }
        }
        nextTickNeurons.addAll(inputNeuronIDs.filter { neuronsWithID[it]!!.active })
        timeStep++
    }

    override var timeStep: Long = 0
        private set

    override fun getFeedback(neuronID: Int): Feedback? {
        return inputNeuronsWithID[neuronID]?.getInternalFeedback() ?: neuronFeedbacks[neuronID]?.value?.let {
            Feedback(it)
        }
    }

    override fun getNeuron(neuronID: Int): Neuron? = neuronsWithID[neuronID]

    override fun getInputNeuron(neuronID: Int): InputNeuron? = inputNeuronsWithID[neuronID]

    private fun touch(sourceID: Int, receiverID: Int) {
        if (receiverID !in nextTickNeurons) {
            val receiver = neuronsWithID[receiverID]!!
            receiver.touch(sourceID, timeStep)
            if (receiver.active) {
                neuronFeedbacks[sourceID]!!.update(receiver.getFeedback(sourceID).value)
                nextTickNeurons.add(receiverID)
            }
        }
    }
}