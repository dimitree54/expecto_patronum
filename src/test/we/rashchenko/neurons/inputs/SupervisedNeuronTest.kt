package we.rashchenko.neurons.inputs

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import we.rashchenko.base.ExternallyControlledActivity
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.zoo.RandomNeuron
import java.util.*

internal class SupervisedNeuronTest {

    @Test
    fun getInternalFeedback() {
        val externallyControlledActivity = ExternallyControlledActivity()
        val baseNeuron = RandomNeuron(0.5f)
        val mirroringNeuron = SupervisedNeuron(externallyControlledActivity, baseNeuron)
        val r = Random()
        repeat(1000){
            externallyControlledActivity.active = r.nextBoolean()
            mirroringNeuron.touch(0, 0)
            assertEquals(mirroringNeuron.active, baseNeuron.active)
            mirroringNeuron.update(Feedback.NEUTRAL, 0)
            if (externallyControlledActivity.active == baseNeuron.active){
                assertEquals(mirroringNeuron.getInternalFeedback(), Feedback.VERY_POSITIVE)
            }
            else{
                assertEquals(mirroringNeuron.getInternalFeedback(), Feedback.VERY_NEGATIVE)
            }
        }
    }
}
