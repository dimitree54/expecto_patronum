package we.rashchenko.neurons

import we.rashchenko.base.Activity
import we.rashchenko.base.Feedback
import we.rashchenko.networks.Evolution
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.networks.controllers.NeuralNetworkController


/**
 * The main element of the [NeuralNetwork]. [Evolution] is targeted to find the fittest [Neuron] where fit function is
 * collaborative [Feedback] from other Neurons. Implementing that interface keep in mind that any implementation valid,
 * but the ones targeted on increasing incoming [Feedback] (by "pleasing" neighbour neurons) have more chances to
 * survive during [Evolution]. Before developing your [Neuron] implementation check [NeuralNetwork] pipeline
 * documentation to understand order in which [Neuron] function called.
 */
interface Neuron : Activity {
	/**
	 * If [touch] called for that [Neuron] it means that incoming signal from other [Neuron] which is active received.
	 * On [touch] current [Neuron] can either continue that activity propagation by also becoming [active]
	 *  or stay not active.
	 * Note that for CPU efficiency if the [Neuron] already [active] it will not receive touches anymore.
	 * In other words we keep touching [Neuron] until the first activation.
	 * @param sourceId Nickname of one of the neighbouring neurons that was [active] on the previous [timeStep]
	 *  and now spreads that activity through [touch] of all its neighbours.
	 * The type of that [Neuron] is unknown because of privacy.
	 * Note that set of neighbours of the current Neuron may change in time ([NeuralNetwork] controls it). So if
	 * touch from some [sourceId] received there probably will be more touches from that [sourceId] in the future.
	 * So, if you [Neuron] behaviour is stateful you may want to save some info between runs.
	 * @param timeStep That is [NeuralNetwork] [timeStep]. Because of CPU efficiency it is not guaranteed that any
	 * [Neuron] function called on each [timeStep].
	 * That parameter introduced for [Neuron] to understand how much time left since last touch
	 *  or between touch and update.
	 */
	fun touch(sourceId: Int, timeStep: Long)

	/**
	 * [NeuralNetwork] have decided to break connection between current [Neuron] and other [Neuron] with [sourceId].
	 * If you have stored some variables for that [sourceId], you can delete it now.
	 */
	fun forgetSource(sourceId: Int)

	/**
	 * Ask current neuron's opinion about the neighbour [Neuron] with [sourceId].
	 * Note that it can be called with any [sourceId] even ones that never appeared in [touch].
	 * In that case just return some default value, for example [Feedback.NEUTRAL].
	 * @return [Feedback] for other neurons that will be considered in their collaborative feedback.
	 */
	fun getFeedback(sourceId: Int): Feedback

	/**
	 * Notifying the current [Neuron] about what [Feedback] it has.
	 * It may be a good idea to change behavior after receiving that [Feedback] trying to maximise that value.
	 *
	 * In most cases [update] called for neuron after it touched some other neurons.
	 * That means that neuron was active on the previous [timeStep], was touching other neurons on that [timeStep]
	 *  and now (on that [timeStep]) update is called.
	 *
	 * But sometime (rarely, because it is slow) [NeuralNetwork] update all its neurons independently on activation
	 *  history.
	 * In that case neurons that never active have chance to change their behaviour.
	 * Because neurons asked about feedback mostly for active neighbours, if neuron active rarely its feedback consist
	 *  not of collaborative feedback from other neurons, but mainly of some external [NeuralNetworkController].
	 * For example there is special [NeuralNetworkController] that sends bad feedback for never active neurons.
	 */
	fun update(feedback: Feedback, timeStep: Long)
}
