package we.rashchenko.environments

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SimpleEnvironmentTest {

	@Test
	fun tick() {
		for (period in listOf(2, 3, 8)) {
			val env = SimpleEnvironment(period)
			assertEquals(env.activities[0].active, env.activities[1].active)
			var numActive = 0
			var numTotal = 0
			var activeOnPrevStep = env.activities[0].active
			repeat(10000) {
				env.tick()
				assertEquals(env.activities[0].active, env.activities[1].active)
				if (it % period == 0) {
					activeOnPrevStep = env.activities[0].active
				} else {
					assertEquals(env.activities[0].active, activeOnPrevStep)
				}
				if (env.activities[0].active) {
					numActive++
				}
				numTotal++
			}
			assertTrue(numActive.toFloat() / numTotal in 0.4f..0.6f)
		}
	}
}