package we.rashchenko.neurons.inputs

import we.rashchenko.base.Feedback
import we.rashchenko.base.HiddenActivity
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.neurons.Neuron

/**
 * Implementation of the [InputNeuron] that copies value of the internal [baseNeuron]
 *  ignoring the [externalActivity].
 * That [externalActivity] is used to calculate [getInternalFeedback].
 * Internal feedback is based on the mismatch of these internal and external activities.
 * That [InputNeuron] is good for training,
 *  but does not allow external activity to initially appear at [NeuralNetwork].
 */
class SupervisedNeuron(
    private val externalActivity: HiddenActivity, private val baseNeuron: Neuron
) : InputNeuron {
    override fun update(feedback: Feedback, timeStep: Long) = baseNeuron.update(
        if (externalActivity.hidden) feedback else getInternalFeedback(), timeStep
    )

    override fun getInternalFeedback(): Feedback {
        return if (externalActivity.hidden) {
            Feedback.NEUTRAL
        } else {
            if (externalActivity.active == baseNeuron.active) Feedback.VERY_POSITIVE else Feedback.VERY_NEGATIVE
        }
    }

    override fun touch(sourceId: Int, timeStep: Long) = baseNeuron.touch(sourceId, timeStep)
    override fun forgetSource(sourceId: Int) = baseNeuron.forgetSource(sourceId)
    override fun getFeedback(sourceId: Int): Feedback = baseNeuron.getFeedback(sourceId)

    override val active: Boolean
        get() = baseNeuron.active
}