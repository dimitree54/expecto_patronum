package we.rashchenko.environments

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

internal class SimpleEnvironmentTest {

    @Test
    fun tick() {
        for (period in listOf(2, 3, 8)) {
            val env = SimpleEnvironment(period)
            assertEquals(env.inputActivities[0].active, env.outputActivities[0].active)
            var numActive = 0
            var numTotal = 0
            var activeOnPrevStep = env.inputActivities[0].active
            repeat(10000) {
                env.tick()
                assertEquals(env.inputActivities[0].active, env.outputActivities[0].active)
                if (it % period == 0) {
                    activeOnPrevStep = env.inputActivities[0].active
                } else {
                    assertEquals(env.inputActivities[0].active, activeOnPrevStep)
                }
                if (env.inputActivities[0].active) {
                    numActive++
                }
                numTotal++
            }
            assertTrue(numActive.toFloat() / numTotal in 0.4f..0.6f)
        }
    }

    @Test
    fun setValidationMode() {
        val env = SimpleEnvironment(1)
        assertFalse(env.outputActivities.first().hidden)
        env.validationMode = true
        assertTrue(env.outputActivities.first().hidden)
        env.validationMode = true
        assertTrue(env.outputActivities.first().hidden)
        env.validationMode = false
        assertFalse(env.outputActivities.first().hidden)
        env.validationMode = false
        assertFalse(env.outputActivities.first().hidden)
    }
}