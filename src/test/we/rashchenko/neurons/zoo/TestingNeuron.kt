package we.rashchenko.neurons.zoo

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.Neuron

class TestingNeuron(
	private val baseNeuron: Neuron,
	private val throwOnDoubleTouch: Boolean,
	private val throwOnActiveAfterUpdate: Boolean
) : Neuron by baseNeuron {
	private var activatedOnTimeStep = -1L
	private var activatedRecently = false
	override fun touch(sourceId: Int, timeStep: Long) {
		if (activatedRecently) {
			throw Exception("Activated recently, but was not updated")
		}
		baseNeuron.touch(sourceId, timeStep)
		if (baseNeuron.active) {
			if (timeStep == activatedOnTimeStep && throwOnDoubleTouch) {
				if (throwOnDoubleTouch) {
					throw Exception("Double touch")
				} else {
					println("Warning, double touch")
				}
			}
			activatedOnTimeStep = timeStep
			activatedRecently = true
		}
	}

	private var lastUpdateTimeStep = -1L
	override fun update(feedback: Feedback, timeStep: Long) {
		baseNeuron.update(feedback, timeStep)
		if (baseNeuron.active && throwOnActiveAfterUpdate) {
			throw Exception("Neuron active after update")
		}
		if (lastUpdateTimeStep == timeStep) {
			throw Exception("Double update")
		}
		lastUpdateTimeStep = timeStep
		if (activatedRecently) {
			if (timeStep != activatedOnTimeStep + 1) {
				throw Exception("Activated, but not updated on the next step.")
			}
			activatedRecently = false
		}
	}
}