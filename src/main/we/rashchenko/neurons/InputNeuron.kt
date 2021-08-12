package we.rashchenko.neurons

import we.rashchenko.utils.Feedback

interface InputNeuron : Neuron {
	fun getInternalFeedback(): Feedback
}