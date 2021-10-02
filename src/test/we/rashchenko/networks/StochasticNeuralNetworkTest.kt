package we.rashchenko.networks

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import we.rashchenko.base.ExternallyControlledActivity
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.inputs.MirroringNeuron
import we.rashchenko.neurons.zoo.RandomNeuronSampler
import java.util.*

internal class StochasticNeuralNetworkTest {
    class SuperExcitedNeuron: Neuron {
        var internalActive = false
        override fun touch(sourceId: Int, timeStep: Long) {
            internalActive = true
        }

        override fun forgetSource(sourceId: Int) {
        }

        override fun getFeedback(sourceId: Int): Feedback {
            return Feedback.VERY_POSITIVE
        }

        override fun update(feedback: Feedback, timeStep: Long) {
            internalActive = false
        }

        override val active: Boolean
            get() = internalActive
    }

    @Test
    fun allFunctionsTest() {
        val externallyControlledActivity = ExternallyControlledActivity()
        externallyControlledActivity.active = true
        val nn = StochasticNeuralNetwork()

        val ids = mutableSetOf<Int>()
        val sourceId = nn.addInputNeuron(MirroringNeuron(externallyControlledActivity, SuperExcitedNeuron()))
        repeat(1000){
            val targetId = nn.add(SuperExcitedNeuron())
            ids.add(targetId)
            nn.addConnection(sourceId, targetId)
        }
        nn.tick()
        nn.tick()
        nn.neuronIDs.forEach{
            assertEquals(nn.getNeuron(it)!!.active, true)
        }

        assertEquals(nn.getFeedback(sourceId), Feedback.VERY_NEGATIVE)

        assertEquals(nn.timeStep, 2)

        nn.connections.keys.forEach { key->
            if (key != sourceId){
                assertEquals(nn.connections[key]!!.size, 0)
            }
        }
        assertTrue(nn.connections[sourceId]!!.toSet() == ids.toSet())

        assertTrue(listOf(sourceId).toSet() == nn.inputNeuronIDs.toSet())

        ids.add(sourceId)
        assertTrue(ids.toSet() == nn.neuronIDs.toSet())

        val deleteId = ids.first()
        nn.remove(deleteId)

        assertTrue(deleteId !in nn.neuronIDs)
        assertTrue(deleteId !in nn.connections[sourceId]!!)
        assertTrue(sourceId in nn.connections.keys)

        nn.remove(sourceId)
        assertTrue(sourceId !in nn.connections.keys)
    }
}
