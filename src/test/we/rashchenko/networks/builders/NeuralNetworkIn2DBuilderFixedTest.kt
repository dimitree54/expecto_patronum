package we.rashchenko.networks.builders

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import we.rashchenko.environments.SimpleEnvironment
import we.rashchenko.networks.StochasticNeuralNetwork
import we.rashchenko.neurons.zoo.RandomNeuronSampler

internal class NeuralNetworkIn2DBuilderFixedTest {

    @Test
    fun testNeuron() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val builder = NeuralNetworkIn2DBuilderFixed(nn, sampler, 5)
        val id = builder.addNeurons(1)[0]
        assertTrue(id in nn.neuronIDs)
        assertTrue(nn.inputNeuronIDs.isEmpty())
        builder.remove(id)
        assertTrue(nn.neuronIDs.isEmpty())
    }

    @Test
    fun testInputOutputEnvironment() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilderFixed(nn, sampler, 5)
        val id = builder.addInputOutputEnvironment(env)
        builder.addNeurons(env.activities.size)
        assertEquals(nn.inputNeuronIDs.size, env.activities.size)
        assertEquals(nn.neuronIDs.size, env.activities.size)
        assertEquals(builder.getEnvironmentOutputNeuronIDs(id)!!.size, 1)
    }

    @Test
    fun getPosition() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilderFixed(nn, sampler, 5)
        builder.addNeurons(1)
        builder.addInputOutputEnvironment(env)
        builder.addNeurons(env.activities.size)
        nn.neuronIDs.forEach {
            assertTrue(builder.getPosition(it) != null)
        }
    }

    @Test
    fun testInputNeuronReplacement() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilderFixed(nn, sampler, 5)
        builder.addInputOutputEnvironment(env)
        builder.addNeurons(env.activities.size)
        val oldIDs = nn.neuronIDs.toSet()
        val oldPositions = oldIDs.map { builder.getPosition(it) }.toSet()
        oldIDs.map { builder.remove(it) }
        assertTrue(nn.inputNeuronIDs.isEmpty())
        assertTrue(nn.neuronIDs.isEmpty())
        builder.addNeurons(env.activities.size)
        assertEquals(nn.inputNeuronIDs.size, env.activities.size)
        assertEquals(nn.neuronIDs.size, env.activities.size)
        val newIDs = nn.neuronIDs.toSet()
        assertNotEquals(oldIDs, newIDs)
        assertEquals(oldPositions, newIDs.map { builder.getPosition(it) }.toSet())
    }

    @Test
    fun getNeuralNetwork() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val builder = NeuralNetworkIn2DBuilderFixed(nn, sampler, 5)
        assertEquals(nn, builder.neuralNetwork)
    }
}
