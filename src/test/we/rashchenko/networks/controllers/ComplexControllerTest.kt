package we.rashchenko.networks.controllers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.ControlledNeuron
import we.rashchenko.neurons.SlowNeuron
import we.rashchenko.neurons.zoo.RandomNeuron

internal class ComplexControllerTest {

    @Test
    fun getControllerFeedbacks() {
        val neurons = listOf(
            ControlledNeuron(RandomNeuron(0.0f)).apply{control=true},
            ControlledNeuron(SlowNeuron(0.0f)).apply{control=true},
            ControlledNeuron(RandomNeuron(0.6f)).apply{control=true},
            ControlledNeuron(SlowNeuron(0.6f)).apply{control=true},
            ControlledNeuron(RandomNeuron(0.9f)).apply{control=true},
            ControlledNeuron(SlowNeuron(0.9f)).apply{control=true},
        )

        repeat(1000){ timeStep->
            neurons.forEach{
                it.touch(timeStep, timeStep.toLong())
                it.update(Feedback.NEUTRAL, timeStep.toLong())
                it.getFeedback(timeStep)
                it.forgetSource(timeStep)
                it.active
            }
        }

        val controllerFeedbacks = ComplexController(ActivityController(), TimeController()).getControllerFeedbacks(neurons)
        assertEquals(
            controllerFeedbacks.zip(controllerFeedbacks.indices).sortedBy { it.first }.map{ it.second },
            listOf(1, 5, 3, 0, 4, 2)
        )
    }
}