package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class WorstNNeuronsTest {
	@Test
	fun testWorstNNeurons() {
		val worstNNeurons = WorstNNeuronIDs(3)
		worstNNeurons.addAll(
			listOf(
				0 to Feedback.VERY_NEGATIVE,
				1 to Feedback.NEUTRAL,
				2 to Feedback.VERY_POSITIVE
			)
		)
		assertTrue(worstNNeurons.size == 3)
		worstNNeurons.add(3 to Feedback.NEUTRAL)
		assertTrue(worstNNeurons.size == 3)

		(4 to Feedback.VERY_POSITIVE).let {
			worstNNeurons.add(it)
			assertFalse(worstNNeurons.contains(it))
		}

		(5 to Feedback.VERY_NEGATIVE).let {
			worstNNeurons.add(it)
			assertTrue(worstNNeurons.contains(it))
		}
	}
}