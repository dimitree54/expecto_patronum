package we.rashchenko.networks.builders

import we.rashchenko.base.Feedback
import we.rashchenko.base.Ticking
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.neurons.Neuron
import we.rashchenko.utils.collections.WorstNNeuronIDs
import java.util.*

/**
 * That is [NeuralNetworkBuilder] that not only adds neurons and connections, but also removes bad neurons.
 * [Evolution] wraps base builder adding [tick] function that implements natural selection.
 * [Evolution] is ticking independently of target [NeuralNetwork] and sometimes
 *  (with [selectionProbability]) collects [Feedback] for all the neurons in the [NeuralNetwork].
 * [Feedback] for non-active neurons is also collected which makes that operation quite expensive,
 *  that is why we do it just sometimes.
 *
 * After collecting [Feedback] for all neurons the worst [neuronsForSelection] of them are considered as candidates for removal.
 * Those neurons are notified by the [Neuron.update] function (calling update considered as a warning).
 * If some [Neuron] warned [warningsBeforeKill] times (in total, not only consequently) it is replaced with other random
 *  [Neuron] by calling [NeuralNetworkBuilder.remove] and [NeuralNetworkBuilder.addNeurons] functions of
 *  the wrapped builder.
 * So, as you can see that class implements only bad neurons killing,
 *  but a sampling of new successful neurons should be managed by the base builder.
 *  @param builder the base builder that manges adding and removal of neurons.
 *  @param neuronsForSelection number of candidates for removal on each selection
 *  @param warningsBeforeKill number of warnings for bad neurons before replacing it
 *  @param selectionProbability probability of the selection on the [tick]
 */
class Evolution(
    builder: NeuralNetworkBuilder,
    private val neuronsForSelection: Int,
    private val warningsBeforeKill: Int,
    private val selectionProbability: Double
) : Ticking, NeuralNetworkBuilder by builder {
    private val warnings = mutableMapOf<Int, Int>()
    private val random = Random()
    override var timeStep: Long = 0
        private set

    override fun tick() {
        if (random.nextDouble() > selectionProbability) {
            return
        }
        val losers = WorstNNeuronIDs(neuronsForSelection)
        neuralNetwork.neuronIDs.forEach { neuronID ->
            val neuronFeedback = neuralNetwork.getFeedback(neuronID)!!
            losers.add(Pair(neuronID, neuronFeedback))
        }
        losers.forEach { (neuronID, feedback) ->
            val newWarningsValue = warnings.getOrDefault(neuronID, 0) + 1
            warnings[neuronID] = newWarningsValue
            if (newWarningsValue > warningsBeforeKill) {
                if (remove(neuronID)) {
                    addNeurons()
                } else {
                    // warning for the bad neuron
                    neuralNetwork.getNeuron(neuronID)!!.update(feedback, neuralNetwork.timeStep)
                }
            }
        }
        timeStep++
    }
}