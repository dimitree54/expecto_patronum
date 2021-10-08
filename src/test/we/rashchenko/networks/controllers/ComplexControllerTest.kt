package we.rashchenko.networks.controllers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.neurons.SlowNeuron
import we.rashchenko.neurons.zoo.RandomNeuron
import java.lang.IllegalArgumentException

internal class ComplexControllerTest {

    @Test
    fun getControllerFeedbacks() {
        val neurons = listOf(
            ControlledNeuron(RandomNeuron(0.0f)).apply { control = true },
            ControlledNeuron(SlowNeuron(0.0f)).apply { control = true },
            ControlledNeuron(RandomNeuron(0.6f)).apply { control = true },
            ControlledNeuron(SlowNeuron(0.6f)).apply { control = true },
            ControlledNeuron(RandomNeuron(0.8f)).apply { control = true },
            ControlledNeuron(SlowNeuron(0.8f)).apply { control = true },
        )

        repeat(1000) { timeStep ->
            neurons.forEach {
                it.touch(timeStep, timeStep.toLong())
                it.update(Feedback.NEUTRAL, timeStep.toLong())
                it.getFeedback(timeStep)
                it.forgetSource(timeStep)
                it.active
            }
        }

        val controllerFeedbacks =
            ComplexController(listOf(ActivityController(), TimeController())).getControllerFeedbacks(neurons)
        assertEquals(
            controllerFeedbacks.indices.sortedBy { controllerFeedbacks[it] },
            listOf(1, 5, 3, 0, 4, 2)
        )
    }

    @Test
    fun testInvalidArguments(){
        assertThrows<IllegalArgumentException>{
            ComplexController(listOf(ActivityController(), TimeController()), listOf(1.0))
        }
        assertThrows<IllegalArgumentException>{
            ComplexController(listOf(ActivityController()), listOf(1.0, 0.0))
        }
        assertThrows<IllegalArgumentException>{
            ComplexController(listOf(), listOf())
        }
    }

    @Test
    fun testWeights(){
        val neurons = listOf(
            ControlledNeuron(RandomNeuron(0.0f)).apply { control = true },
            ControlledNeuron(RandomNeuron(0.6f)).apply { control = true },
            ControlledNeuron(RandomNeuron(0.8f)).apply { control = true }
        )

        repeat(1000) { timeStep ->
            neurons.forEach {
                it.touch(timeStep, timeStep.toLong())
                it.update(Feedback.NEUTRAL, timeStep.toLong())
            }
        }

        assertEquals(
            ComplexController(listOf(TimeController(), ActivityController()), listOf(0.0, 1.0)).getControllerFeedbacks(neurons),
            ActivityController().getControllerFeedbacks(neurons)
        )

        assertEquals(
            Feedback.VERY_POSITIVE,
            ComplexController(listOf(ActivityController()), listOf(1000.0)).getControllerFeedbacks(neurons)[1],
        )
    }

}
