package we.rashchenko.networks.builders

import we.rashchenko.base.ObservableActivities
import we.rashchenko.environments.Environment
import we.rashchenko.environments.InputOutputEnvironment
import we.rashchenko.environments.SimpleEnvironment
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.networks.NeuralNetworkWithInput
import we.rashchenko.neurons.inputs.InputNeuron

/**
 * [NeuralNetworkWithInput] builder. Extends [NeuralNetworkBuilder] with ability to connect [Environment] or any other
 *  [ObservableActivities].
 */
interface NeuralNetworkWithInputBuilder : NeuralNetworkBuilder {
    /**
     * [NeuralNetworkWithInput] that [NeuralNetworkWithInputBuilder] builds.
     */
    override val neuralNetwork: NeuralNetworkWithInput

    /**
     * Connect [InputOutputEnvironment] (for example [SimpleEnvironment]) to the [NeuralNetwork].
     * Connecting means wrapping all input and output activities with [InputNeuron] and adding them to the network.
     * Kind of [InputNeuron] may be different for input and output neurons.
     * @return unique id for that environment.
     */
    fun addInputOutputEnvironment(environment: InputOutputEnvironment): Int

    /**
     * Get [InputNeuron] IDs connected to [InputOutputEnvironment.outputActivities] of the environment
     *  with the specified [environmentID].
     * @return list of requested neurons or null if there is no [Environment] with [environmentID].
     *  Note, if [Environment] under [environmentID] is not [InputOutputEnvironment], then empty list should be returned
     * */
    fun getEnvironmentOutputNeuronIDs(environmentID: Int): List<Int>?
}