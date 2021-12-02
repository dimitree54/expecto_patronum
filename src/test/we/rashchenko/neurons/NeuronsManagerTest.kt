package we.rashchenko.neurons

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.zoo.RandomNeuronSampler

internal class NeuronsManagerTest {

    @Test
    fun add() {
        val neuronsManager = NeuronsManager().apply {
            add(RandomNeuronSampler(0.1f))
            add(RandomNeuronSampler(0.2f))
            add(RandomNeuronSampler(0.3f))
            add(RandomNeuronSampler(0.4f))
        }
        assertThrows<IllegalArgumentException> {
            neuronsManager.add(RandomNeuronSampler(0.4f))
        }
    }

    @Test
    fun nextIllegal() {
        val neuronsManager = NeuronsManager()
        assertThrows<Exception> {
            neuronsManager.next(0)
        }
    }

    @Test
    fun next() {
        val neuronsManager = NeuronsManager().apply {
            add(RandomNeuronSampler(0.0f))
            add(RandomNeuronSampler(1.0f))
        }
        repeat(1000) {
            val neuron = neuronsManager.next(it)
            neuron.touch(0, 0)
            if (neuron.active) {
                neuronsManager.reportFeedback(it, Feedback.VERY_POSITIVE)
            } else {
                neuronsManager.reportFeedback(it, Feedback.VERY_NEGATIVE)
            }
            neuron.update(Feedback.NEUTRAL, 0)
        }
        var numActive = 0
        var numPassive = 0
        repeat(100000) {
            val neuron = neuronsManager.next(-it - 1)
            neuron.touch(0, 0)
            if (neuron.active) {
                numActive++
            } else {
                numPassive++
            }
            neuron.update(Feedback.NEUTRAL, 0)
        }
        assertTrue(numActive.toDouble() / (numActive + numPassive) > 0.2)
        assertTrue(numPassive.toDouble() / (numActive + numPassive) < 0.8)
    }

    @Test
    fun reportFeedback() {
        val neuronsManager = NeuronsManager().apply {
            add(RandomNeuronSampler(0.1f))
            add(RandomNeuronSampler(0.2f))
            add(RandomNeuronSampler(0.3f))
            add(RandomNeuronSampler(0.4f))
        }
        assertThrows<IllegalArgumentException> {
            neuronsManager.reportFeedback(-1, Feedback.VERY_NEGATIVE)
        }
        neuronsManager.next(1).also { neuronsManager.reportFeedback(1, Feedback.VERY_NEGATIVE) }
    }

    @Test
    fun reportDeath() {
        val neuronsManager = NeuronsManager().apply {
            add(RandomNeuronSampler(0.1f))
            add(RandomNeuronSampler(0.2f))
            add(RandomNeuronSampler(0.3f))
            add(RandomNeuronSampler(0.4f))
        }
        assertThrows<IllegalArgumentException> {
            neuronsManager.reportDeath(-1)
        }
        neuronsManager.next(1).also { neuronsManager.reportDeath(1) }
    }

    @Test
    fun getSummary() {
        val neuronsManager = NeuronsManager().apply {
            add(RandomNeuronSampler(0.0f))
            add(RandomNeuronSampler(1.0f))
        }
        println(getSummary(neuronsManager.getSamplerStats()))
        println(getResultsMarkdownTable(neuronsManager.getSamplerStats()))
        repeat(1000) {
            val neuron = neuronsManager.next(it)
            neuron.touch(0, 0)
            if (neuron.active) {
                neuronsManager.reportFeedback(it, Feedback.VERY_POSITIVE)
            } else {
                neuronsManager.reportFeedback(it, Feedback.VERY_NEGATIVE)
            }
            neuron.update(Feedback.NEUTRAL, 0)
        }
        println(getSummary(neuronsManager.getSamplerStats()))
        println(getResultsMarkdownTable(neuronsManager.getSamplerStats()))
    }
}