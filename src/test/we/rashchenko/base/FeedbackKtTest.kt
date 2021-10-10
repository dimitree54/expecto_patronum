package we.rashchenko.base

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class FeedbackKtTest {

    @Test
    fun getAccuracy() {
        assertEquals(
            1.0,
            getAccuracy(listOf(Feedback.VERY_POSITIVE, Feedback.VERY_POSITIVE, Feedback.VERY_POSITIVE))
        )
        assertEquals(
            0.0,
            getAccuracy(listOf(Feedback.VERY_NEGATIVE, Feedback.VERY_NEGATIVE, Feedback.VERY_NEGATIVE))
        )
        assertEquals(
            0.5,
            getAccuracy(listOf(Feedback.VERY_NEGATIVE, Feedback.VERY_POSITIVE))
        )
        assertEquals(
            0.5,
            getAccuracy(listOf(Feedback(0.1), Feedback.NEUTRAL))
        )
    }
}