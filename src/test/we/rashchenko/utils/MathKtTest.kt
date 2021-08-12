package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MathKtTest {
	private val eps = 0.00001

	@Test
	fun testClip() {
		assertEquals(5.0.clip(), 1.0, eps)
		assertEquals((-5.0).clip(), 0.0, eps)
		assertEquals((-5.0).clip(-1.0, 1.0), -1.0, eps)
		assertEquals(5.0.clip(-2.0, -1.0), -1.0, eps)
	}

	@Test
	fun testSoftmax() {
		softmax(listOf(0.0, 0.0, 0.0, 0.0)).zip(listOf(0.25, 0.25, 0.25, 0.25)).forEach {
			assertEquals(it.first, it.second, eps)
		}
		softmax(listOf(0.0, 0.0, 0.69314, 0.0)).zip(listOf(0.2, 0.2, 0.4, 0.2)).forEach {
			assertEquals(it.first, it.second, eps)
		}
		softmax(listOf(0.69314, 0.0, 0.69314, 0.0)).zip(listOf(1.0 / 3, 1.0 / 6, 1.0 / 3, 1.0 / 6)).forEach {
			assertEquals(it.first, it.second, eps)
		}
	}
}