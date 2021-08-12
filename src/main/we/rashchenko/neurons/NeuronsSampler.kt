package we.rashchenko.neurons

import we.rashchenko.utils.Feedback

interface NeuronsSampler {
	val name: String
	fun next(id: Int): Neuron
	fun reportFeedback(id: Int, feedback: Feedback)
	fun reportDeath(id: Int)
}
