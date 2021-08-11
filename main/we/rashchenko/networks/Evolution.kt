package we.rashchenko.networks

import we.rashchenko.networks.builders.NeuralNetworkBuilder
import we.rashchenko.utils.WorstNNeuronIDs
import java.util.*

class Evolution(
	private val builder: NeuralNetworkBuilder,
	private val neuronsForSelection: Int,
	private val warningsBeforeKill: Int,
	private val stepProbability: Double
) {
	private val warnings = mutableMapOf<Int, Int>()
	private val random = Random()
	fun step() {
		if (random.nextDouble() > stepProbability) {
			return
		}
		val losers = WorstNNeuronIDs(neuronsForSelection)
		builder.neuralNetwork.neuronIDs.forEach { neuronID ->
			val neuronFeedback = builder.neuralNetwork.getFeedback(neuronID)!!
			builder.reportFeedback(neuronID, neuronFeedback)
			losers.add(Pair(neuronID, neuronFeedback))
		}
		losers.forEach { (neuronID, feedback) ->
			val newWarningsValue = warnings.getOrDefault(neuronID, 0) + 1
			warnings[neuronID] = newWarningsValue
			if (newWarningsValue > warningsBeforeKill) {
				if (builder.remove(neuronID)) {
					builder.addNeuron()
				} else {
					// warning for the bad neuron
					builder.neuralNetwork.getNeuron(neuronID)?.update(feedback, builder.neuralNetwork.timeStep)
				}
			}
		}
	}
}