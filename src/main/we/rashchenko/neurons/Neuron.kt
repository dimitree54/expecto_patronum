package we.rashchenko.neurons

import we.rashchenko.base.Activity
import we.rashchenko.utils.Feedback


interface Neuron : Activity {

	/**
	 * If that called it means the neuron received incoming signal from other neuron with sourceId.
	 * The set of possible incoming sourceId is dynamic and may change with time.
	 * Here active property may be changed.
	 */
	fun touch(sourceId: Int, timeStep: Long): Boolean

	/**
	 * External controller have decided to break connection between this neuron and other neuron with sourceId.
	 * If you have stored some variables for that sourceId, you can delete it.
	 */
	fun forgetSource(sourceId: Int)

	/**
	 * External controller asks neuron's opinion about other neuron with sourceId.
	 */
	fun getFeedback(sourceId: Int): Feedback

	/**
	 * Called by external controller. Here you can change weights based on feedback.
	 * Usually it will be called when active==true, so active neuron can train itself.
	 * But if it called anf active==false it means that this neuron activates too rarely and become candidate for
	 *  pruning. In that case you can try to do something (for example increase activation probability) to avoid that.
	 */
	fun update(feedback: Feedback, timeStep: Long)
}
