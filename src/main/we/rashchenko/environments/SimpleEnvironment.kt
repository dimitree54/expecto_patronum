package we.rashchenko.environments

import we.rashchenko.base.ExternallyControlledActivity
import java.util.*

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