package we.rashchenko.neurons

import we.rashchenko.base.Feedback

interface NeuronsSampler {
	val name: String
	fun next(id: Int): Neuron
	fun reportFeedback(id: Int, feedback: Feedback)
	fun reportDeath(id: Int)
}
