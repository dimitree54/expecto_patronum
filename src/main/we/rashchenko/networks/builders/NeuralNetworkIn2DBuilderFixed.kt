package we.rashchenko.networks.builders

import com.google.common.collect.HashBiMap
import we.rashchenko.base.Activity
import we.rashchenko.base.HiddenActivity
import we.rashchenko.environments.InputOutputEnvironment
import we.rashchenko.networks.NeuralNetworkWithInput
import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.NeuronsSampler
import we.rashchenko.neurons.inputs.MirroringNeuron
import we.rashchenko.neurons.inputs.SupervisedNeuron
import we.rashchenko.utils.IDsGenerator
import we.rashchenko.utils.KNearestVectorsConnectionSampler
import we.rashchenko.utils.RandomPositionSampler
import we.rashchenko.utils.Vector2


/**
 * [NeuralNetworkBuilder] that places [NeuralNetworkWithInput] into 2D space.
 * After adding neurons their positions and connections fixed.
 * Removing some neuron leaves vacant place (including connections) that will be taken by the next added neuron.
 * @param neuralNetwork [NeuralNetworkWithInput] to build
 * @param neuronsSampler generator of new [Neuron]s for [neuralNetwork]
 */
class NeuralNetworkIn2DBuilderFixed(
    override val neuralNetwork: NeuralNetworkWithInput,
    private val neuronsSampler: NeuronsSampler,
    kNeighbours: Int
) : NeuralNetworkWithInputBuilder {
    private val ids = IDsGenerator()
    private val positionSampler: Iterator<Vector2> = RandomPositionSampler()
    private val vectorsConnectionSampler = KNearestVectorsConnectionSampler(kNeighbours)

    // Note that because of privacy we can not use the same ids as in NN
    private val nnIDs2builderIDs = mutableMapOf<Int, Int>()

    private val environmentIDWithHiddenActivities = mutableMapOf<Int, List<HiddenActivity>>()
    private val unattachedActivities = mutableListOf<Activity>()
    private val unattachedHiddenActivities = mutableListOf<HiddenActivity>()

    private val vacantPositions = mutableListOf<Vector2>()

    private val positionsWithActivities = mutableMapOf<Vector2, Activity>()
    private val positionsWithHiddenActivities = HashBiMap.create<Vector2, HiddenActivity>()
    private val positionsWithNeuronId = HashBiMap.create<Vector2, Int>()

    private val incomingConnections = mutableMapOf<Vector2, MutableList<Vector2>>()
    private val outcomingConnections = mutableMapOf<Vector2, MutableList<Vector2>>()

    private fun addNeuronWithoutConnection(): Int {
        val position =
            if (unattachedActivities.isNotEmpty() || unattachedHiddenActivities.isNotEmpty() || vacantPositions.isEmpty()) {
                positionSampler.next()
            } else {
                vacantPositions.removeLast()
            }

        if (unattachedActivities.isNotEmpty()) {
            positionsWithActivities[position] = unattachedActivities.removeLast()
        } else if (unattachedHiddenActivities.isNotEmpty()) {
            positionsWithHiddenActivities[position] = unattachedHiddenActivities.removeLast()
        }

        val builderId = ids.next()
        val baseNeuron = sample(builderId)

        val neuronId =
            positionsWithActivities[position]?.let {
                neuralNetwork.addInputNeuron(MirroringNeuron(it, baseNeuron))
            } ?: positionsWithHiddenActivities[position]?.let {
                neuralNetwork.addInputNeuron(SupervisedNeuron(it, baseNeuron))
            } ?: neuralNetwork.add(baseNeuron)

        nnIDs2builderIDs[neuronId] = builderId
        positionsWithNeuronId[position] = neuronId

        return neuronId
    }

    private fun connect(ids: Set<Int>) {
        ids.forEach { id ->
            val positionTo = positionsWithNeuronId.inverse()[id]!!
            val inConnections = incomingConnections[positionTo] ?: vectorsConnectionSampler.connectNew(
                positionTo,
                positionsWithNeuronId.keys
            ).also {
                incomingConnections[positionTo] = it.toMutableList()
                it.forEach { positionFrom ->
                    outcomingConnections.getOrPut(positionFrom) { mutableListOf() }.add(positionTo)
                }
            }
            val outConnections = outcomingConnections[positionTo] ?: emptyList()

            inConnections.forEach {
                neuralNetwork.addConnection(positionsWithNeuronId[it]!!, id)
            }
            outConnections.forEach {
                val idTo = positionsWithNeuronId[it]!!
                if (!ids.contains(idTo)) {
                    neuralNetwork.addConnection(id, idTo)
                }
            }
        }
    }

    override fun addNeurons(n: Int): List<Int> {
        val ids = (0 until n).map { addNeuronWithoutConnection() }
        connect(ids.toSet())
        return ids
    }

    override fun addInputOutputEnvironment(environment: InputOutputEnvironment): Int {
        unattachedActivities.addAll(environment.inputActivities)
        unattachedHiddenActivities.addAll(environment.outputActivities)
        val id = ids.next()
        environmentIDWithHiddenActivities[id] = environment.outputActivities
        return id
    }

    override fun getEnvironmentOutputNeuronIDs(environmentID: Int): List<Int>? =
        environmentIDWithHiddenActivities[environmentID]?.map { positionsWithHiddenActivities.inverse()[it] }
            ?.mapNotNull { positionsWithNeuronId[it]!! }

    override fun remove(neuronID: Int): Boolean {
        val position = positionsWithNeuronId.inverse().remove(neuronID) ?: return false
        vacantPositions.add(position)
        neuralNetwork.remove(neuronID)
        nnIDs2builderIDs.remove(neuronID)
        return true
    }

    /**
     * The function that reveals 2d coordinates of neurons. Useful for visualisations.
     */
    fun getPosition(neuronID: Int): Vector2? = positionsWithNeuronId.inverse()[neuronID]

    private var lastSamplerUpdateTimeStep = -1L
    private fun sample(builderId: Int): Neuron {
        if (lastSamplerUpdateTimeStep != neuralNetwork.timeStep) {
            lastSamplerUpdateTimeStep = neuralNetwork.timeStep
            updateSampler()
        }
        return neuronsSampler.next(builderId)
    }

    private fun updateSampler() {
        nnIDs2builderIDs.forEach { (neuronID, builderID) ->
            neuronsSampler.reportFeedback(builderID, neuralNetwork.getFeedback(neuronID)!!)
        }
    }
}
