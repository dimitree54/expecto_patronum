package we.rashchenko.networks.builders

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import we.rashchenko.environments.SimpleEnvironment
import we.rashchenko.networks.StochasticNeuralNetwork
import we.rashchenko.neurons.zoo.RandomNeuronSampler

internal class NeuralNetworkIn2DBuilderTest {

    @Test
    fun testNeuron() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        val id = builder.addNeuron()
        assertTrue(id in nn.neuronIDs)
        assertTrue(nn.inputNeuronIDs.isEmpty())
        builder.remove(id)
        assertTrue(nn.neuronIDs.isEmpty())
    }

    @Test
    fun testEnvironment() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        val id = builder.addEnvironment(env)
        assertEquals(nn.inputNeuronIDs.size, env.activities.size)
        assertEquals(nn.neuronIDs.size, env.activities.size)
        assertTrue(builder.getEnvironmentOutputNeuronIDs(id)!!.isEmpty())
        builder.removeEnvironment(id)
        assertTrue(nn.inputNeuronIDs.isEmpty())
        assertTrue(nn.neuronIDs.isEmpty())
        assertEquals(builder.getEnvironmentOutputNeuronIDs(id), null)
    }

    @Test
    fun testInputOutputEnvironment() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        val id = builder.addInputOutputEnvironment(env)
        assertEquals(nn.inputNeuronIDs.size, env.activities.size)
        assertEquals(nn.neuronIDs.size, env.activities.size)
        assertEquals(builder.getEnvironmentOutputNeuronIDs(id)!!.size, 1)
        builder.removeEnvironment(id)
        assertTrue(nn.inputNeuronIDs.isEmpty())
        assertTrue(nn.neuronIDs.isEmpty())
        assertEquals(builder.getEnvironmentOutputNeuronIDs(id), null)
    }

    @Test
    fun getPosition() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        builder.addNeuron()
        builder.addEnvironment(env)
        nn.neuronIDs.forEach {
            assertTrue(builder.getPosition(it) != null)
        }
    }

    @Test
    fun testInputNeuronReplacement() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val env = SimpleEnvironment(5)
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        builder.addEnvironment(env)
        val oldIDs = nn.neuronIDs.toSet()
        val oldPositions = oldIDs.map { builder.getPosition(it) }.toSet()
        oldIDs.map { builder.remove(it) }
        assertTrue(nn.inputNeuronIDs.isEmpty())
        assertTrue(nn.neuronIDs.isEmpty())
        repeat(env.activities.size) { builder.addNeuron() }
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
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        assertEquals(nn, builder.neuralNetwork)
    }
}
