package we.rashchenko.neurons

import we.rashchenko.base.Feedback
import we.rashchenko.networks.NeuralNetwork

/**
 * It is special kind of neuron that is controlled from outside of [NeuralNetwork], so it does not consider
 *  collaborative feedback from inside the [NeuralNetwork],
 *  but can determine its [Feedback] itself in [getInternalFeedback] function.
 * Ability to determine its own feedback is kind of cheating in sense of Evolution, so be careful engineering such
 *  neurons.
 *
 * Also, that neuron is always observable by [NeuralNetwork].
 * Making some neuron being input you mark that it can change its state not only due to [touch] or [update], but also
 *  spontaneously.
 * So the [NeuralNetwork] on each tick checks that neuron status.
 */
interface InputNeuron : Neuron {
	fun getInternalFeedback(): Feedback
}