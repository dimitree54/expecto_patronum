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
     * Connect [ObservableActivities] (for example [Environment]) to the [NeuralNetwork].
     * Connecting means wrapping all [ObservableActivities] with [InputNeuron] and adding them to the network.
     * @return unique id for that environment. That id can be later used to [removeEnvironment]
     */
    fun addEnvironment(environment: ObservableActivities): Int

    /**
     * Connect [InputOutputEnvironment] (for example [SimpleEnvironment]) to the [NeuralNetwork].
     * Connecting means wrapping all input and output activities with [InputNeuron] and adding them to the network.
     * Kind of [InputNeuron] may be different for input and output neurons.
     * @return unique id for that environment. That id can be later used to [removeEnvironment]
     */
    fun addInputOutputEnvironment(environment: InputOutputEnvironment): Int

    /**
     * Remove connection of the [Environment] under [environmentID] by removing all its [InputNeuron]s.
     * @return true if environment was successfully remove, false if there is not such [environmentID]
     */
    fun removeEnvironment(environmentID: Int): Boolean
}