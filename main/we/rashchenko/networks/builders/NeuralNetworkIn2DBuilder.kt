package we.rashchenko.networks.builders

import we.rashchenko.base.Activity
import we.rashchenko.environments.Environment
import we.rashchenko.networks.NeuralNetworkWithInput
import we.rashchenko.neurons.MirroringNeuron
import we.rashchenko.neurons.Neuron
import we.rashchenko.neurons.NeuronsSampler
import we.rashchenko.utils.*


class NeuralNetworkIn2DBuilder(
	override val neuralNetwork: NeuralNetworkWithInput,
	private val neuronsSampler: NeuronsSampler
) : NeuralNetworkBuilder {
	private val positionSampler: Iterator<Vector2> = RandomPositionSampler()
	private val vectorsConnectionSampler = KNearestVectorsConnectionSampler(5)
	private val positionsWithNNIDs = mutableMapOf<Vector2, Int>()
	private val nnIDsWithPosition = mutableMapOf<Int, Vector2>()

	// Note that because of privacy we can not use the same ids as in NN
	private val nnIDs2builderIDs = mutableMapOf<Int, Int>()

	private val unattachedActivitiesWithPosition = mutableListOf<Pair<Activity, Vector2>>()
	private fun addNeuronWithoutConnection(): Int {
		val builderID = randomIds.next()
		val neuron: Neuron = neuronsSampler.next(builderID)
		return if (unattachedActivitiesWithPosition.isEmpty()) {
			neuralNetwork.add(neuron).also { neuronID ->
				addNeuronWithoutConnection(neuronID, builderID, positionSampler.next())
			}
		} else {
			val (activity, position) = unattachedActivitiesWithPosition.removeLast()
			neuralNetwork.addInputNeuron(MirroringNeuron(activity, neuron)).also { neuronID ->
				addNeuronWithoutConnection(neuronID, builderID, position)
				neuronIDsConnectedToActivity[neuronID] = activity
			}
		}

	}

	private fun addNeuronWithoutConnection(neuronID: Int, builderID: Int, position: Vector2) {
		positionsWithNNIDs[position] = neuronID
		nnIDsWithPosition[neuronID] = position
		nnIDs2builderIDs[neuronID] = builderID
	}

	private fun addNeuronsWithoutConnection(n: Int): List<Int> = (0 until n).map { addNeuronWithoutConnection() }

	private val neuronIDsConnectedToActivity = mutableMapOf<Int, Activity>()
	private fun addEnvironmentWithoutConnection(environment: Environment): List<Int> {
		val neuronIDs = mutableListOf<Int>()
		environment.activities.associateWith { positionSampler.next() }
			.forEach { (activity, position) ->
				val builderID = randomIds.next()
				val neuron = MirroringNeuron(activity, neuronsSampler.next(builderID))
				val nnID = neuralNetwork.addInputNeuron(neuron)
				addNeuronWithoutConnection(nnID, builderID, position)

				neuronIDsConnectedToActivity[nnID] = activity
				neuronIDs.add(nnID)
			}
		return neuronIDs
	}

	private fun connectAll() {
		val connections = vectorsConnectionSampler.connectAll(positionsWithNNIDs.keys)
		connections.forEach { (fromPosition, toPositions) ->
			toPositions.forEach { toPosition ->
				neuralNetwork.addConnection(
					positionsWithNNIDs[fromPosition]!!, positionsWithNNIDs[toPosition]!!
				)
			}
		}
	}

	fun initialise(numberOfNeurons: Int, environment: Environment) {
		addNeuronsWithoutConnection(numberOfNeurons)
		addEnvironmentWithoutConnection(environment)
		connectAll()
	}

	override fun remove(neuronID: Int): Boolean {
		return neuralNetwork.remove(neuronID).also { removed ->
			if (removed) {
				val builderID = nnIDs2builderIDs[neuronID]!!
				val position = nnIDsWithPosition[neuronID]!!
				if (neuronID in neuronIDsConnectedToActivity) {
					unattachedActivitiesWithPosition.add(
						neuronIDsConnectedToActivity[neuronID]!! to nnIDsWithPosition[neuronID]!!
					)
					neuronIDsConnectedToActivity.remove(neuronID)
				}

				neuronsSampler.reportDeath(builderID)
				positionsWithNNIDs.remove(position)
				nnIDsWithPosition.remove(neuronID)
				nnIDs2builderIDs.remove(neuronID)
			}
		}
	}

	private fun connect(position: Vector2) {
		val newConnections = vectorsConnectionSampler.connectNew(position, nnIDsWithPosition.values)
		newConnections.forEach { (fromPosition, toPositions) ->
			toPositions.forEach { toPosition ->
				neuralNetwork.addConnection(
					positionsWithNNIDs[fromPosition]!!, positionsWithNNIDs[toPosition]!!
				)
			}
		}
	}

	override fun addNeuron(): Int =
		addNeuronWithoutConnection().also { neuronID ->
			connect(nnIDsWithPosition[neuronID]!!)
		}

	fun getPosition(neuronID: Int): Vector2? = nnIDsWithPosition[neuronID]

	override fun reportFeedback(neuronID: Int, feedback: Feedback) {
		nnIDs2builderIDs[neuronID]?.let { id ->
			neuronsSampler.reportFeedback(id, feedback)
		}
	}
}
