package we.rashchenko.neurons

import we.rashchenko.base.Feedback

/**
 * Factory for the [Neuron].
 * Each neuron implementation should have such factory.
 * That factory can store and use the information (received by [reportFeedback] and [reportDeath])
 *  about all previously sampled
 *  neurons of that type to sample more successful neurons in the future.
 *
 * Warning for contest: it is prohibited to save references to previously sampled neurons inside the [NeuronsSampler].
 * To save meta-info with which [Neuron] was sampled use some separate classes.
 * We need that restriction to not let the [NeuronsSampler] to modify previously sampled neurons.
 * [Neuron] should use only local information.
 * For more info about such restrictions check [ChNN competition rules](https://dimitree54.github.io/rules/),
 * [Loopholes discussion](https://github.com/dimitree54/ChNN/discussions/45#discussion-3583606).
 * There you can suggest solutions how to integrate such restriction into the code architecture instead of text rules.
 */
interface NeuronsSampler {
    /**
     * Unique name of your sampler.
     * Check naming conventions at [ChNN competition rules](https://dimitree54.github.io/rules/).
     */
    val name: String

    /**
     * Sample the next [Neuron].
     * @param id Under that id [reportFeedback] and [reportDeath] will report about how good the sampled [Neuron] is.
     * @return sampled neuron. Note that storing reference to the returned [Neuron]
     *  is prohibited by [ChNN competition rules](https://dimitree54.github.io/rules/).
     */
    fun next(id: Int): Neuron

    /**
     * Report about how good the [Neuron] with [id] is.
     * Using that info [NeuronsSampler] can modify behavior to sample more successful neurons in the future.
     */
    fun reportFeedback(id: Int, feedback: Feedback)

    /**
     * Report that the [Neuron] with [id] was so bad that could not stand natural selection and died.
     * You can remove saved info about that [Neuron] now
     *  (it is guaranteed that there will be no [reportFeedback] for that [id] anymore).
     * Or you can save info about that [Neuron] to not sample such bad neurons in the future.
     */
    fun reportDeath(id: Int)
}
