package we.rashchenko.utils

import we.rashchenko.base.Activity
import we.rashchenko.environments.InputOutputEnvironment
import we.rashchenko.networks.builders.NeuralNetworkWithInputBuilder

/**
 * In classical supervised setting you need to calculate metrics of how neural network perform.
 * In ChNN framework supervised setting represented with [InputOutputEnvironment].
 * That class compares [InputOutputEnvironment.outputActivities] with corresponding nn input neurons and
 *  calculate metrics based on that.
 * @param environmentID id of the environment to calculate metrics for.
 *  That environment had to be already added to nnBuilder, otherwise IllegalArgumentException will be thrown
 * @param nnBuilder builder that contains environment with environmentID
 */
class MetricsCalculator(
    environmentID: Int,
    nnBuilder: NeuralNetworkWithInputBuilder
) {
    private val targetActivities: List<Activity>
    private val predictedActivities: List<Activity>

    init {
        val (targetActivities, predictedActivities) = nnBuilder.getEnvironmentOutputNeuronIDs(environmentID)
            ?.map { id ->
                nnBuilder.neuralNetwork.getInputNeuron(id)!!.let { it.externalActivity to it.baseNeuron }
            }?.unzip()
            ?: throw IllegalArgumentException("Unknown environment id")
        this.targetActivities = targetActivities
        this.predictedActivities = predictedActivities
    }

    val accuracy: Double
        get() {
            return predictedActivities.indices.count {
                targetActivities[it].active == predictedActivities[it].active
            }.toDouble() / predictedActivities.size
        }
}