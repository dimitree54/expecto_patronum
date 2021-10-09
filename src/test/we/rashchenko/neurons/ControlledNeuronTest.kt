package we.rashchenko.neurons

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.zoo.RandomNeuron
import java.lang.Thread.sleep

class SlowNeuron(activationProbability: Float = 0.5f): RandomNeuron(activationProbability){
    override fun touch(sourceId: Int, timeStep: Long) {
        sleep(1)
        super.touch(sourceId, timeStep)
    }

    override fun update(feedback: Feedback, timeStep: Long) {
        sleep(1)
        super.update(feedback, timeStep)
    }

    override fun forgetSource(sourceId: Int) {
        sleep(1)
        super.forgetSource(sourceId)
    }

    override fun getFeedback(sourceId: Int): Feedback {
        sleep(1)
        return super.getFeedback(sourceId)
    }

    override val active: Boolean
        get() {
            sleep(1)
            return super.active
        }
}

internal class ControlledNeuronTest {

    @Test
    fun getStats() {
        val controlledNeuron = ControlledNeuron(SlowNeuron())

        assertEquals(controlledNeuron.control, false)
        controlledNeuron.control = true
        assertEquals(controlledNeuron.control, true)

        // checking default values
        assertEquals(controlledNeuron.numControlledGetActive, 0)
        assertEquals(controlledNeuron.numControlledForget, 0)
        assertEquals(controlledNeuron.numControlledUpdate, 0)
        assertEquals(controlledNeuron.numControlledTouch, 0)
        assertEquals(controlledNeuron.numControlledGetFeedback, 0)
        assertEquals(controlledNeuron.averageActivity, 0.0)
        assertEquals(controlledNeuron.averageGetActiveTime, 0.0)
        assertEquals(controlledNeuron.averageGetFeedbackTime, 0.0)
        assertEquals(controlledNeuron.averageUpdateTime, 0.0)
        assertEquals(controlledNeuron.averageTouchTime, 0.0)
        assertEquals(controlledNeuron.averageForgetTime, 0.0)

        repeat(1000){
            controlledNeuron.touch(it, it.toLong())
            controlledNeuron.update(Feedback.NEUTRAL, it.toLong())
            controlledNeuron.getFeedback(it)
            controlledNeuron.forgetSource(it)
            controlledNeuron.active
        }
        assertEquals(controlledNeuron.numControlledGetActive, 2000)  // note that ControlledNeuron.update also uses getActive
        assertEquals(controlledNeuron.numControlledForget, 1000)
        assertEquals(controlledNeuron.numControlledUpdate, 1000)
        assertEquals(controlledNeuron.numControlledTouch, 1000)
        assertEquals(controlledNeuron.numControlledGetFeedback, 1000)
        assertTrue(controlledNeuron.averageActivity in 0.4..0.6)
        assertTrue(controlledNeuron.averageGetActiveTime in 1.0..2.0)
        assertTrue(controlledNeuron.averageGetFeedbackTime in 1.0..2.0)
        assertTrue(controlledNeuron.averageUpdateTime in 1.0..2.0)
        assertTrue(controlledNeuron.averageTouchTime in 1.0..2.0)
        assertTrue(controlledNeuron.averageForgetTime in 1.0..2.0)
    }
}
