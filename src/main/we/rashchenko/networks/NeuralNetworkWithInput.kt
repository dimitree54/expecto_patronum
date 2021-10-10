package we.rashchenko.networks

import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.inputs.InputNeuron
import we.rashchenko.utils.MetricsCalculator

/**
 * [NeuralNetwork] with some [Neuron]-nodes specially marked as [InputNeuron].
 * [NeuralNetworkWithInput] should check [InputNeuron]s state on each [tick] to not miss any [InputNeuron]
 *  state change initiated from outside the [NeuralNetwork].
 *
 * To understand what privileges [InputNeuron] has check its documentation.
 */
interface NeuralNetworkWithInput : NeuralNetwork {
    /**
     * IDs for only input neurons.
     */
    val inputNeuronIDs: Collection<Int>

    /**
     * Add [InputNeuron] node to the graph.
     *
     * Calling that function with some [Neuron] you notify the [NeuralNetworkWithInput] that the [Neuron] can change
     *  its state not only due to [Neuron.touch] or [Neuron.update], but also spontaneously.
     * So the [NeuralNetworkWithInput] on each tick should check that [Neuron] status.
     *
     * @return newly generated id for the added [InputNeuron].
     * Now that [InputNeuron] will be referred with that id in [neuronIDs] and [connections]
     */
    fun addInputNeuron(neuron: InputNeuron): Int

    /**
     * Get the [InputNeuron] with [neuronID]. Note that this method reveals [Neuron] privacy so be careful using it.
     * Do not let the system become discriminative based on [Neuron]'s type.
     * We need that method to control or observe neurons from outside the [NeuralNetwork].
     * For example, it is used by [MetricsCalculator]
     * @return [InputNeuron] hidden under [neuronID]. Returns null if there is no such ID at [NeuralNetwork] or [Neuron]
     *  under that [neuronID] is not [InputNeuron]
     */
    fun getInputNeuron(neuronID: Int): InputNeuron?
}