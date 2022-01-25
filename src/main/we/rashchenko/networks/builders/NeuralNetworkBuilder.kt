package we.rashchenko.networks.builders

import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.neurons.Neuron

/**
 * [NeuralNetwork] manages internal neurons' behavior,
 *  but it is not supposed to manage new neurons sampling for itself,
 *  nor managing how neurons should be wired with each other.
 * For that purpose there is [NeuralNetworkBuilder].
 */
interface NeuralNetworkBuilder {
    /**
     * [NeuralNetwork] that [NeuralNetworkBuilder] builds.
     */
    val neuralNetwork: NeuralNetwork

    /**
     * Sample [n] random [Neuron]s, add them to the [neuralNetwork] and add
     * some connections between each other and other previously added neurons
     * @return list ids of the newly added [Neuron]s
     */
    fun addNeurons(n: Int = 1): List<Int>

    /**
     * Remove the [Neuron] under [neuronID] from [NeuralNetwork]
     * @return true if deletion successful, false if there is no such [neuronID] at [neuralNetwork]
     */
    fun remove(neuronID: Int): Boolean
}