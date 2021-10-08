package we.rashchenko.networks.controllers

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.utils.clip
import java.lang.IllegalArgumentException

/**
 * [NeuralNetworkController] that calls one or more other [controllers]
 *  and average their [getControllerFeedbacks] output.
 * @param controllers non-empty list of NeuralNetworkController
 * @param weights double list with the same size as controllers (or null to equal weights for all controllers).
 *  It is recommended to use list of weights that sum in 1.
 *  Otherwise, resulting feedback can be out of bounds. In that case it will be clipped.
 */
class ComplexController(private val controllers: List<NeuralNetworkController>, weights: List<Double>? = null) :
    NeuralNetworkController {
	private val weights = weights ?: List(controllers.size){ 1.0 / controllers.size }
    init {
        if (controllers.isEmpty()) {
            throw IllegalArgumentException("no controllers provided")
        }
        if (controllers.size != this.weights.size) {
            throw IllegalArgumentException("controller and weight lists size does not match")
        }
    }

    override fun getControllerFeedbacks(neurons: List<ControlledNeuron>): List<Feedback> {
        val feedbacksPerController = controllers.map { it.getControllerFeedbacks(neurons) }
        return neurons.indices.map { i ->
            Feedback(
                feedbacksPerController.indices.sumOf { j ->
                    weights[j] * feedbacksPerController[j][i].value
                }.clip(-1.0, 1.0)
            )
        }
    }
}