package we.rashchenko.networks

import we.rashchenko.networks.controllers.NeuralNetworkController
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.neurons.InputNeuron
import we.rashchenko.neurons.Neuron
import we.rashchenko.utils.Feedback
import java.util.*

class ControlledNeuralNetwork(
	private val baseNeuralNetwork: NeuralNetworkWithInput,
	private val controller: NeuralNetworkController,
	private val auditProbability: Double,
	private val updateControllerFeedbackPeriod: Long,
	private val controllerFeedbackWeight: Double
) : NeuralNetworkWithInput by baseNeuralNetwork {
	private val controlledNeuronsWithID = mutableMapOf<Int, ControlledNeuron>()
	private val controllerFeedbacks = mutableMapOf<Int, Feedback>()

	override fun add(neuron: Neuron): Int {
		return ControlledNeuron(neuron, timeStep).let {
			baseNeuralNetwork.add(it).also { id ->
				controlledNeuronsWithID[id] = it
				controllerFeedbacks[id] = Feedback.NEUTRAL
			}
		}
	}

	override fun addInputNeuron(neuron: InputNeuron): Int {
		// NOTE: we do not control input neurons as they anyway ignore external feedback, just specifying default
		return baseNeuralNetwork.addInputNeuron(neuron).also { neuronID ->
			controllerFeedbacks[neuronID] = Feedback.NEUTRAL
		}
	}

	override fun remove(neuronID: Int): Boolean {
		return baseNeuralNetwork.remove(neuronID).also { removed ->
			if (removed) {
				controlledNeuronsWithID.remove(neuronID)
				controllerFeedbacks.remove(neuronID)
			}
		}
	}

	private val random = Random()
	private var control = false
	override fun tick() {
		if (control) {
			control = false
			controlledNeuronsWithID.values.forEach { it.control = false }
		}
		if (random.nextDouble() < auditProbability) {
			control = true
			controlledNeuronsWithID.values.forEach { it.control = true }
		}
		baseNeuralNetwork.tick()
		if (timeStep % updateControllerFeedbackPeriod == 0L) {
			val (neuronIDsList, controlledNeuronsList) = controlledNeuronsWithID.toList().unzip()
			val feedbacks = controller.getControllerFeedbacks(controlledNeuronsList, timeStep)
			neuronIDsList.forEachIndexed { i, id ->
				controllerFeedbacks[id] = feedbacks[i]
			}
		}
	}

	override fun getFeedback(neuronID: Int): Feedback? {
		return getControllerFeedback(neuronID)?.let { controllerFeedback ->
			getCollaborativeFeedback(neuronID)?.let { collaborativeFeedback ->
				Feedback(
					collaborativeFeedback.value * (1 - controllerFeedbackWeight) +
							controllerFeedback.value * controllerFeedbackWeight
				)
			}
		}
	}

	fun getControllerFeedback(neuronID: Int): Feedback? = controllerFeedbacks[neuronID]
	fun getCollaborativeFeedback(neuronID: Int): Feedback? = baseNeuralNetwork.getFeedback(neuronID)
}
