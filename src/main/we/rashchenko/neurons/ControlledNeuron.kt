package we.rashchenko.neurons

import we.rashchenko.base.Feedback
import we.rashchenko.networks.controllers.NeuralNetworkController
import we.rashchenko.utils.ExponentialMovingAverageHotStart
import we.rashchenko.utils.ZERO_DIV_EPS
import kotlin.system.measureTimeMillis

/**
 * Wrapper for any other [baseNeuron] that reveals some of its statistics,
 *  for example [averageActivity] or [averageTouchTime].
 * That statistics can be used by [NeuralNetworkController] to calculate external [Feedback] for that [Neuron]
 */
class ControlledNeuron(private val baseNeuron: Neuron) : Neuron by baseNeuron {
	/**
	 * Switcher whether control all [baseNeuron] functions or run as is.
	 */
	var control: Boolean = false

	/**
	 * How many times [active] getter was called while being under [control]
	 */
	var numControlledGetActive: Int = 0
		private set
	private val emaGetActiveTime = ExponentialMovingAverageHotStart()

	/**
	 * Average [active] getter runtime. Note that average calculated using [ExponentialMovingAverageHotStart].
	 *  Time measured and considered in EMA only if call was under [control].
	 */
	val averageGetActiveTime: Double
		get() = emaGetActiveTime.value
	override val active: Boolean
		get() {
			return if (control) {
				numControlledGetActive++
				val result: Boolean
				measureTimeMillis {
					result = baseNeuron.active
				}.let { emaGetActiveTime.update(it.toDouble()) }
				result
			} else {
				baseNeuron.active
			}
		}

	private val emaUpdateTime = ExponentialMovingAverageHotStart()

	/**
	 * How many times [update] was called while being under [control]
	 */
	var numControlledUpdate: Int = 0
		private set
	private var numControlledUpdateActive: Int = 0

	/**
	 * Rate of [update] calls when [baseNeuron] was active over all [control]led [update] calls
	 */
	val averageActivity: Double
		get() = numControlledUpdateActive.toDouble() / (numControlledUpdate + ZERO_DIV_EPS)

	/**
	 * Average [update] runtime. Note that average calculated using [ExponentialMovingAverageHotStart].
	 *  Time measured and considered in EMA only if call was under [control].
	 */
	val averageUpdateTime: Double
		get() = emaUpdateTime.value
	override fun update(feedback: Feedback, timeStep: Long) {
		if (control) {
			numControlledUpdate++
			if (active) {
				numControlledUpdateActive++
			}
			measureTimeMillis {
				baseNeuron.update(feedback, timeStep)
			}.let { emaUpdateTime.update(it.toDouble()) }
		} else {
			baseNeuron.update(feedback, timeStep)
		}
	}

	private val emaForgetTime = ExponentialMovingAverageHotStart()

	/**
	 * How many times [forgetSource] was called while being under [control]
	 */
	var numControlledForget: Int = 0
		private set

	/**
	 * Average [forgetSource] runtime. Note that average calculated using [ExponentialMovingAverageHotStart].
	 *  Time measured and considered in EMA only if call was under [control].
	 */
	val averageForgetTime: Double
		get() = emaForgetTime.value
	override fun forgetSource(sourceId: Int) {
		if (control) {
			numControlledForget++
			measureTimeMillis {
				baseNeuron.forgetSource(sourceId)
			}.let { emaForgetTime.update(it.toDouble()) }
		} else {
			baseNeuron.forgetSource(sourceId)
		}
	}


	/**
	 * How many times [getFeedback] was called while being under [control]
	 */
	var numControlledGetFeedback: Int = 0
		private set
	private val emaGetFeedbackTime = ExponentialMovingAverageHotStart()

	/**
	 * Average [getFeedback] runtime. Note that average calculated using [ExponentialMovingAverageHotStart].
	 *  Time measured and considered in EMA only if call was under [control].
	 */
	val averageGetFeedbackTime: Double
		get() = emaGetFeedbackTime.value
	override fun getFeedback(sourceId: Int): Feedback {
		return if (control) {
			numControlledGetFeedback++
			val result: Feedback
			measureTimeMillis {
				result = baseNeuron.getFeedback(sourceId)
			}.let { emaGetFeedbackTime.update(it.toDouble()) }
			result
		} else {
			baseNeuron.getFeedback(sourceId)
		}
	}


	/**
	 * How many times [touch] was called while being under [control]
	 */
	var numControlledTouch: Int = 0
		private set
	private val emaTouchTime = ExponentialMovingAverageHotStart()

	/**
	 * Average [touch] runtime. Note that average calculated using [ExponentialMovingAverageHotStart].
	 *  Time measured and considered in EMA only if call was under [control].
	 */
	val averageTouchTime: Double
		get() = emaTouchTime.value
	override fun touch(sourceId: Int, timeStep: Long) {
		if (control) {
			numControlledTouch++
			measureTimeMillis {
				baseNeuron.touch(sourceId, timeStep)
			}.let { emaTouchTime.update(it.toDouble()) }
		} else {
			baseNeuron.touch(sourceId, timeStep)
		}
	}
}
