package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FeedbackTest {
	@Test
	fun testConstructor() {
		assertThrows<IllegalArgumentException> {
			Feedback(-2.0)
		}
		assertThrows<IllegalArgumentException> {
			Feedback(2.0)
		}
	}

	@Test
	fun testConstants() {
		assertTrue(Feedback(-1.0) == Feedback.VERY_NEGATIVE)
		assertTrue(Feedback(0.0) == Feedback.NEUTRAL)
		assertTrue(Feedback(1.0) == Feedback.VERY_POSITIVE)
	}

	@Test
	fun testCompareTo() {
		assertTrue(Feedback.VERY_NEGATIVE < Feedback.NEUTRAL)
		assertTrue(Feedback.VERY_NEGATIVE < Feedback.VERY_POSITIVE)
		assertFalse(Feedback.VERY_NEGATIVE > Feedback.NEUTRAL)
		assertFalse(Feedback.VERY_NEGATIVE > Feedback.VERY_POSITIVE)

		assertTrue(Feedback.VERY_NEGATIVE <= Feedback.NEUTRAL)
		assertTrue(Feedback.VERY_NEGATIVE <= Feedback.VERY_POSITIVE)
		assertFalse(Feedback.VERY_NEGATIVE >= Feedback.NEUTRAL)
		assertFalse(Feedback.VERY_NEGATIVE >= Feedback.VERY_POSITIVE)

		assertTrue(Feedback.VERY_NEGATIVE >= Feedback.VERY_NEGATIVE)
		assertTrue(Feedback.VERY_NEGATIVE <= Feedback.VERY_NEGATIVE)
	}
}