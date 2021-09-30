package we.rashchenko.neurons.inputs

import we.rashchenko.base.Feedback
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.neurons.Neuron

/**
 * Special kind of neuron that is controlled from outside the [NeuralNetwork].
 * It does not consider collaborative feedback from inside the [NeuralNetwork],
 *  but can determine its own [Feedback] by itself in [getInternalFeedback] function.
 * Ability to determine its own feedback is kind of cheating in sense of Evolution, so be careful engineering such
 *  neurons.
 */
interface InputNeuron : Neuron {
	/**
	 * Estimate its own feedback.
	 */
	fun getInternalFeedback(): Feedback
}