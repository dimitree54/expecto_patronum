package we.rashchenko.neurons.inputs

import we.rashchenko.base.Activity
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.Neuron


/**
 * Implementation of the [InputNeuron] that copies value of the external [Activity]
 *  ignoring all the internal [baseNeuron] activity.
 * That internal [baseNeuron] activity is used to calculate [getInternalFeedback].
 * Internal feedback is based on the mismatch of these internal and external activities.
 * That [InputNeuron] is good for receiving always available external activity,
 *  but bad for training on sometimes missing activity.
 */
class MirroringNeuron(
    private val externalActivity: Activity, private val baseNeuron: Neuron
) : InputNeuron {
    override fun update(feedback: Feedback, timeStep: Long) = baseNeuron.update(getInternalFeedback(), timeStep)

    override fun getInternalFeedback(): Feedback =
        if (externalActivity.active == baseNeuron.active) Feedback.VERY_POSITIVE else Feedback.VERY_NEGATIVE

    override fun touch(sourceId: Int, timeStep: Long) = baseNeuron.touch(sourceId, timeStep)
    override fun forgetSource(sourceId: Int) = baseNeuron.forgetSource(sourceId)
    override fun getFeedback(sourceId: Int): Feedback = baseNeuron.getFeedback(sourceId)

    override val active: Boolean
        get() = externalActivity.active
}