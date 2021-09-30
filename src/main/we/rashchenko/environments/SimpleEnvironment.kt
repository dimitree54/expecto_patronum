package we.rashchenko.environments

import we.rashchenko.base.ExternallyControlledActivity
import java.util.*

/**
 * Exemplar simple environment that have just 2 public nodes.
 * Nodes randomly change their behaviour once in [tickPeriod] ticks.
 * These two nodes always either both active or not active (they are synchronised).
 */
class SimpleEnvironment(private val tickPeriod: Int) : Environment {
	override val activities = List(2) { ExternallyControlledActivity() }
	override var timeStep: Long = 0
		private set

	private val random = Random()
	override fun tick() {
		if (timeStep % tickPeriod == 0L) {
			val newValue = random.nextBoolean()
			activities.forEach {
				it.active = newValue
			}
		}
		timeStep++
	}
}