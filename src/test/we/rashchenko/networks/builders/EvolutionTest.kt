package we.rashchenko.networks.builders

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import we.rashchenko.networks.StochasticNeuralNetwork
import we.rashchenko.neurons.zoo.RandomNeuronSampler

internal class EvolutionTest {

    @Test
    fun tick() {
        val nn = StochasticNeuralNetwork()
        val sampler = RandomNeuronSampler()
        val builder = NeuralNetworkIn2DBuilder(nn, sampler)
        val evolution = Evolution(builder, 2, 1, 1.0)
        repeat(3){evolution.addNeuron()}
        val oldIDs = nn.neuronIDs.toSet()
        evolution.tick()
        assertEquals(oldIDs, nn.neuronIDs.toSet())
        evolution.tick()
        val newIDs = nn.neuronIDs.toSet()
        assertEquals(oldIDs.intersect(newIDs).size, 1)
    }
}