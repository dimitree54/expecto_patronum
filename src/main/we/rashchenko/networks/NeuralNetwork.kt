package we.rashchenko.networks

import we.rashchenko.base.Feedback
import we.rashchenko.base.Ticking
import we.rashchenko.networks.builders.Evolution
import we.rashchenko.neurons.Neuron

/**
 * [NeuralNetwork] is a directed graph with [Neuron] as nodes.
 * However, for [privacy reasons](https://github.com/dimitree54/ChNN-Library/discussions/18#discussion-3583508),
 *  all that neurons are hidden under their IDs.
 * Based on these graph connections neurons "touch" each other
 *  (check [Neuron] documentation to understand what "touch" is)
 *  (source [Neuron] of the connection "touches" target [Neuron] of the connection).
 * After the touch, there is a feedback collection stage where neurons estimate each other quality based
 *  on reversed connections (the target node of the connection sends feedback to the source node).
 */
interface NeuralNetwork : Ticking {
	/**
	 * IDs of all the neurons. Can be considered as nodes of the [NeuralNetwork] graph
	 */
	val neuronIDs: Collection<Int>

	/**
	 * Edges of the [NeuralNetwork] graph.
	 */
	val connections: Map<Int, Collection<Int>>

	/**
	 * Add [Neuron] node to the graph.
	 * @return newly generated id for the added [Neuron].
	 * Now that [Neuron] will be referred with that id in [neuronIDs] and [connections]
	 */
	fun add(neuron: Neuron): Int

	/**
	 * Remove the [Neuron] with specific [neuronID] from that [NeuralNetwork] and remove all connections it has
	 *  (both from and to that [Neuron]).
	 * @return true if the removal is successful; false if there is no such [neuronID] in [neuronIDs].
	 */
	fun remove(neuronID: Int): Boolean

	/**
	 * Add connection to the graph by specifying source and target nodes of the connection.
	 * @return true if the connection adding is successful;
	 *  false if one or both of [fromNeuronID] and [toNeuronID] are missing
	 */
	fun addConnection(fromNeuronID: Int, toNeuronID: Int): Boolean

	/**
	 * Get current [Feedback] of the [Neuron] with [neuronID].
	 * That [Feedback] represent how well that [Neuron] works in a team of all [NeuralNetwork] nodes.
	 * @return Feedback or null if there is no [Neuron] with that [neuronID] in the [NeuralNetwork].
	 */
	fun getFeedback(neuronID: Int): Feedback?

	/**
	 * Get the [Neuron] with [neuronID]. Note that this method reveals [Neuron] privacy so be careful using it.
	 * Do not let the system become discriminative based on [Neuron]'s type.
	 * We need that method to control or observe neurons from outside the [NeuralNetwork].
	 * For example, it is used by [Evolution] or for nn visualizer app (outside that project).
	 * @return [Neuron] hidden under [neuronID] or null if there is no such ID at [NeuralNetwork]
	 */
	fun getNeuron(neuronID: Int): Neuron?
}

