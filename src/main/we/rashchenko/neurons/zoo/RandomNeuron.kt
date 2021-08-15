package we.rashchenko.neurons.zoo

import we.rashchenko.base.Feedback
import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.NeuronsSampler
import java.util.*

/**
 * Exemplar simple implementation of the [Neuron] interface.
 */
class RandomNeuron(val activationProbability: Float) : Neuron {
	private val random = Random()

	/**
	 * That dummy neuron does not remember any of its neighbours and just activates with some probability
	 * [activationProbability] (different between neurons of that type) on touch. So it does not use nor [sourceId]
	 * nor [timeStep]
	 */
	override fun touch(sourceId: Int, timeStep: Long) {
		if (random.nextFloat() < activationProbability) {
			active = true
		}
	}

	/**
	 * As this neuron remember nothing, it has nothing to forget.
	 */
	override fun forgetSource(sourceId: Int) {}

	/**
	 * That neuron just returns random feedback for any [sourceId]
	 */
	override fun getFeedback(sourceId: Int): Feedback {
		return Feedback(random.nextDouble() * 2 - 1)
	}

	/**
	 * Update mean that there will be no more [touch] on that [timeStep]. So to prepare for the next [timeStep]
	 * that random neuron turns back into default not active state.
	 */
	override fun update(feedback: Feedback, timeStep: Long) {
		active = false
	}

	override var active: Boolean = false
		private set
}


/**
 * Exemplar sampler for the [RandomNeuron]. For demonstrating purposes that neurons sampler is stateful: more successful
 * neuron was, more likely similar neuron will be sampled.
 */
class RandomNeuronSampler : NeuronsSampler {
	override val name: String = "RandomNeuronSampler"

	private val random = Random()
	private val neurons = mutableMapOf<Int, RandomNeuron>()
	private val feedbacks = mutableMapOf<Int, Feedback>()

	/**
	 * In 50% cases return the copy of most successful neuron previously sampled,
	 * in other 50% cases return [RandomNeuron] with random parameter.
	 * Note that the maximum activationProbability is 0.2 to leave some chances to stay not active
	 * (note that there may be several touches on each timeStep)
	 */
	override fun next(id: Int): Neuron {
		val neuron = if (feedbacks.isEmpty() || random.nextBoolean()) {
			RandomNeuron(random.nextFloat() * 0.2f)
		} else {
			RandomNeuron(neurons[feedbacks.maxByOrNull { it.value.value }!!.key]!!.activationProbability)
		}
		neurons[id] = neuron
		return neuron
	}

	/**
	 * Saving reported feedback to know what neuron was the most successful.
	 */
	override fun reportFeedback(id: Int, feedback: Feedback) {
		feedbacks[id] = feedback
	}

	/**
	 * Clearing memory from dead neurons because now that information useless.
	 */
	override fun reportDeath(id: Int) {
		feedbacks.remove(id)
		neurons.remove(id)
	}
}
