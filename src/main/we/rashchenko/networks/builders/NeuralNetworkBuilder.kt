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
     * Sample random [Neuron], add it to the [neuralNetwork] and add
     * some connections to and from that new [Neuron]
     * @return id of the new [Neuron]
     */
    fun addNeuron(): Int

    /**
     * Remove the [Neuron] under [neuronID] from [NeuralNetwork]
     * @return true if deletion successful, false if there is no such [neuronID] at [neuralNetwork]
     */
    fun remove(neuronID: Int): Boolean
}