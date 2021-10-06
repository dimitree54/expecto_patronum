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

        assertEquals(controlledNeuron.getAverageTime(), 0.0)
        assertEquals(controlledNeuron.getAverageActivity(), 0.0)

        repeat(1000){
            controlledNeuron.touch(it, it.toLong())
            controlledNeuron.update(Feedback.NEUTRAL, it.toLong())
            controlledNeuron.getFeedback(it)
            controlledNeuron.forgetSource(it)
            controlledNeuron.active
        }
        assertTrue(controlledNeuron.getAverageTime() in 5.0..10.0)
        assertTrue(controlledNeuron.getAverageActivity() in 0.4..0.6)
    }
}
