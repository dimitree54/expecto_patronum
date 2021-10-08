package we.rashchenko.networks.controllers

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.neurons.zoo.RandomNeuron

internal class ActivityControllerTest {

    @Test
    fun getControllerFeedbacks() {
        val neurons = listOf(
            ControlledNeuron(RandomNeuron(0f)).apply{control=true},
            ControlledNeuron(RandomNeuron(0.5f)).apply{control=true},
            ControlledNeuron(RandomNeuron(0.5f)).apply{control=true},
            ControlledNeuron(RandomNeuron(0.5f)).apply{control=true},
            ControlledNeuron(RandomNeuron(1f)).apply{control=true},
        )

        repeat(1000){ timeStep->
            neurons.forEach{
                it.touch(timeStep, timeStep.toLong())
                it.update(Feedback.NEUTRAL, timeStep.toLong())
            }
        }

        val controllerFeedbacks = ActivityController().getControllerFeedbacks(neurons)
        assertTrue(controllerFeedbacks[0] < Feedback(-0.8))
        assertTrue(controllerFeedbacks[1] > Feedback(0.8))
        assertTrue(controllerFeedbacks[2] > Feedback(0.8))
        assertTrue(controllerFeedbacks[3] > Feedback(0.8))
        assertTrue(controllerFeedbacks[4] < Feedback(-0.8))
    }
}
