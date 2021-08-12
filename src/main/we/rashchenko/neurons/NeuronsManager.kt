package we.rashchenko.neurons

import we.rashchenko.utils.ExponentialMovingAverage
import we.rashchenko.utils.Feedback
import we.rashchenko.utils.softmax
import we.rashchenko.utils.update
import java.util.*

class NeuronsManager : NeuronsSampler {
	override val name: String = "manager"
	private val neuronSamplerMap = mutableMapOf<Int, NeuronsSampler>()
	private val samplersScore = mutableMapOf<NeuronsSampler, ExponentialMovingAverage>()
	private val probabilityRanges = mutableMapOf<NeuronsSampler, ClosedFloatingPointRange<Double>>()
	private val random = Random()

	private val defaultScore: Feedback = Feedback.NEUTRAL
	fun add(sampler: NeuronsSampler) {
		if (samplersScore.keys.any { it.name == sampler.name }) {
			throw IllegalArgumentException("Sampler with that name already registered at NeuronsManager")
		}
		samplersScore[sampler] = ExponentialMovingAverage(defaultScore.value)
		updateRanges()
	}

	private fun updateRanges() {
		val keys = samplersScore.keys
		val probabilities = softmax(samplersScore.values.map { it.value })

		probabilityRanges.clear()
		var lastMax = 0.0
		keys.mapIndexed { index, neuronsSampler ->
			val newMax = lastMax + probabilities[index]
			probabilityRanges[neuronsSampler] = lastMax..newMax
			lastMax = newMax
		}
	}

	override fun next(id: Int): Neuron {
		random.nextDouble().let { randomValue ->
			probabilityRanges.forEach { (sampler, probabilityRange) ->
				if (randomValue in probabilityRange) {
					return sampler.next(id).also { neuronSamplerMap[id] = sampler }
				}
			}
		}
		throw Exception("no neuron samplers added to manager")
	}

	override fun reportFeedback(id: Int, feedback: Feedback) {
		val sampler = neuronSamplerMap[id] ?: throw IllegalArgumentException("Unknown neuron")
		sampler.reportFeedback(id, feedback)
		samplersScore[sampler]?.update(feedback) ?: throw Exception("Invalid manager state")
		updateRanges()
	}

	override fun reportDeath(id: Int) {
		val sampler = neuronSamplerMap[id] ?: throw IllegalArgumentException("Unknown neuron")
		sampler.reportDeath(id)
		neuronSamplerMap.remove(id)
	}

	fun getSummary(): String {
		val summary = StringBuilder("Neurons manager summary:")
		samplersScore.forEach { (sampler, score) ->
			val probability = probabilityRanges[sampler]!!.let { it.endInclusive - it.start }
			summary.append(
				"\n\t${sampler.name} has score ${"%.${2}f".format(score.value)} " +
						"and have ${"%.${2}f".format(probability)} probability to be chosen next time"
			)
		}
		return summary.toString()
	}
}