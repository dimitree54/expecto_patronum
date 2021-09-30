package we.rashchenko.neurons

import we.rashchenko.base.Activity
import we.rashchenko.base.Feedback
import we.rashchenko.networks.NeuralNetwork
import we.rashchenko.networks.StochasticNeuralNetwork
import we.rashchenko.networks.builders.Evolution
import we.rashchenko.networks.controllers.NeuralNetworkController


/**
 * The main element of the [NeuralNetwork]. [Evolution] is targeted to find the fittest [Neuron] where the fit function is
 * collaborative [Feedback] from other Neurons. Implementing that interface keep in mind that any implementation is valid,
 * but the ones targeted on increasing incoming [Feedback] (by "pleasing" neighbor neurons) have more chances to
 * survive during [Evolution]. Before developing your [Neuron] implementation check [NeuralNetwork] pipeline
 * documentation to understand the order in which [Neuron] function is called.
 * Note that documentation for the [Neuron] considers that [StochasticNeuralNetwork] or [NeuralNetwork] with similar
 *  rules (optimized for sparse activations) used.
 * Examples of such prejudice are that [Neuron] is touched until the first activation or that [update] is called after
 *  the [touch].
 * We use that assumption because [StochasticNeuralNetwork] is the only implementation of the [NeuralNetwork] so far
 *  and to help participants of the ChNN contest to implement effective [Neuron] for [StochasticNeuralNetwork].
 * But if you are going to use the different [NeuralNetwork] and think that such assumptions should not be in
 *  the documentation please open an issue to discuss that.
 */
interface Neuron : Activity {
	/**
	 * If [touch] is called for that [Neuron] it means that an incoming signal from another [Neuron] (which is
	 *  active) is received.
	 * On [touch], current [Neuron] can either continue that activity propagation by also becoming [active]
	 *  or stay not active.
	 * Note that for CPU efficiency if the [Neuron] is already [active] it will not receive
	 *  touches anymore on that [timeStep].
	 * In other words, we keep touching the [Neuron] only until the first activation.
	 * @param sourceId The nickname of one of the neighboring neurons that was [active] on the previous [timeStep]
	 *  and now spreads that activity by [touch]ing all neighbors.
	 * The type of source [Neuron] is unknown because of privacy, so only anonymous integer id received.
	 * Note that the set of neighbours of the current Neuron may change in time ([NeuralNetwork] controls it).
	 * So if touch from some [sourceId] received there probably will be more touches from that [sourceId] in the future
	 *  (until [forgetSource] called for that id).
	 * So, if your [Neuron] behavior is stateful you may want to save some info between touches.
	 * @param timeStep That is [NeuralNetwork] time stamp.
	 * Because of CPU efficiency it may happen that for some neurons no functions called on some time steps.
	 * Consider that developing your [Neuron].
	 * That parameter introduced for [Neuron] to understand how much time left since last touch
	 *  or between touch and update.
	 */
	fun touch(sourceId: Int, timeStep: Long)

	/**
	 * [NeuralNetwork] has decided to break the connection between the current [Neuron] and other [Neuron] with [sourceId].
	 * If you have stored some variables for that [sourceId], you can delete it now.
	 */
	fun forgetSource(sourceId: Int)

	/**
	 * Ask current neuron's opinion about the neighbor [Neuron] with [sourceId].
	 * Note that it can be called with any [sourceId] even ones that never appeared in [touch].
	 * In that case just return some default value, for example, [Feedback.NEUTRAL].
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
