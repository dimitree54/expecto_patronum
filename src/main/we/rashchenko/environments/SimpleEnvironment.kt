package we.rashchenko.environments

import we.rashchenko.base.ExternallyControlledActivity
import we.rashchenko.base.ExternallyControlledHiddenActivity
import java.util.*

/**
 * Exemplar simple environment that have just 2 public nodes, one input and one output.
 * Nodes randomly change their behaviour once in [tickPeriod] ticks.
 * These two nodes always either both active or not active (they are synchronised).
 */
class SimpleEnvironment(private val tickPeriod: Int) : InputOutputEnvironment {
	private val size = 1
	override val inputActivities = List(size) { ExternallyControlledActivity() }
	override val outputActivities = List(size) { ExternallyControlledHiddenActivity() }
	override var timeStep: Long = 0
		private set

	private val random = Random()
	override fun tick() {
		if (timeStep % tickPeriod == 0L) {
			for (i in 0 until size){
				val newValue = random.nextBoolean()
				inputActivities[i].active = newValue
				outputActivities[i].active = newValue
			}
		}
		timeStep++
	}
}