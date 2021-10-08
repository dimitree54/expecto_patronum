package we.rashchenko.networks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import we.rashchenko.base.ExternallyControlledActivity
import we.rashchenko.base.Feedback
import we.rashchenko.networks.controllers.ActivityController
import we.rashchenko.neurons.inputs.MirroringNeuron
import we.rashchenko.neurons.zoo.RandomNeuron

internal class ControlledNeuralNetworkTest {

    @Test
    fun testAll() {
        val nn = ControlledNeuralNetwork(
            StochasticNeuralNetwork(),
            ActivityController(),
            0.5, 2, 0.5
        )
        val neurons = listOf(
            RandomNeuron(0.0f),
            RandomNeuron(0.6f),
            RandomNeuron(0.9f),
        )
        val ids = neurons.map{nn.add(it)}

        val externallyControlledActivity = ExternallyControlledActivity().apply{active=true}
        val sourceId = nn.addInputNeuron(MirroringNeuron(externallyControlledActivity, RandomNeuron(1f)))
        ids.forEach{nn.addConnection(sourceId, it)}

        repeat(1000){
            nn.tick()
        }

        ids.forEach{ assertEquals(nn.getInternalFeedback(it), Feedback.NEUTRAL) }

        assertEquals(
            ids.indices.sortedBy {nn.getFeedback(ids[it])},
            listOf(0, 2, 1)
        )
        assertEquals(
            ids.indices.sortedBy {nn.getExternalFeedback(ids[it])},
            listOf(0, 2, 1)
        )

        nn.remove(ids.first())
        assertEquals(nn.getExternalFeedback(ids.first()), null)
        assertEquals(nn.getInternalFeedback(ids.first()), null)
        assertEquals(nn.getFeedback(ids.first()), null)
    }
}
