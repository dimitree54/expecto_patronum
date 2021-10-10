package we.rashchenko.neurons.inputs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import we.rashchenko.base.ExternallyControlledHiddenActivity
import we.rashchenko.base.Feedback
import we.rashchenko.neurons.zoo.RandomNeuron
import java.util.*

internal class SupervisedNeuronTest {

    @Test
    fun getInternalFeedback() {
        val externallyControlledActivity = ExternallyControlledHiddenActivity()
        val baseNeuron = RandomNeuron(0.5f)
        val supervisedNeuron = SupervisedNeuron(externallyControlledActivity, baseNeuron)
        val r = Random()
        repeat(1000) {
            externallyControlledActivity.active = r.nextBoolean()
            supervisedNeuron.touch(0, 0)
            assertEquals(supervisedNeuron.active, baseNeuron.active)
            supervisedNeuron.update(Feedback.NEUTRAL, 0)
            if (externallyControlledActivity.active == baseNeuron.active) {
                assertEquals(supervisedNeuron.getInternalFeedback(), Feedback.VERY_POSITIVE)
            } else {
                assertEquals(supervisedNeuron.getInternalFeedback(), Feedback.VERY_NEGATIVE)
            }
        }
    }
}
