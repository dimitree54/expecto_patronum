package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExponentialMovingAverageHotStartTest {
    private val eps = 0.00001

    @Test
    fun testEMA() {
        ExponentialMovingAverageHotStart(0.5).let {
            assertEquals(it.value, 0.0, eps)  // testing default value
            it.update(-8.0)
            assertEquals(it.value, -8.0, eps)  // hot start: after one value it copies it
            it.update(8.0)
            assertEquals(it.value, 2.6666666666666665, eps)
            it.update(8.0)
            assertEquals(it.value, 5.714285714285714, eps)
            it.update(8.0)
            assertEquals(it.value, 6.933333333333334, eps)
            it.update(8.0)
            assertEquals(it.value, 7.483870967741935, eps)
            it.update(8.0)
            assertEquals(it.value, 7.746031746031746, eps)
        }
        ExponentialMovingAverageHotStart(0.99).let {
            assertEquals(it.value, 0.0, eps)  // testing default value
            it.update(-8.0)
            assertEquals(it.value, -8.0, eps)  // hot start: after one value it copies it
            it.update(8.0)
            assertEquals(it.value, 0.04020100502512532, eps)
            it.update(8.0)
            assertEquals(it.value, 2.72017777179219, eps)
            it.update(8.0)
            assertEquals(it.value, 4.060098482412566, eps)
            it.update(8.0)
            assertEquals(it.value, 4.863996774401935, eps)
            it.update(8.0)
            assertEquals(it.value, 5.3998838611764945, eps)
        }
    }

    @Test
    fun testDefaultEPS() {
        assertEquals(
            ExponentialMovingAverageHotStart(0.99).run {
                update(0.0)
                update(100.0)
                value
            },
            ExponentialMovingAverageHotStart().run {
                update(0.0)
                update(100.0)
                value
            }
        )
    }
}