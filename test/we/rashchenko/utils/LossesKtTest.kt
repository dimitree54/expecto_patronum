package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LossesKtTest {
	@Test
	fun testHemmingDistance() {
		assertEquals(
			hemmingDistance(
				listOf(false, false, false, false),
				listOf(false, false, false, false)
			),
			0.0
		)
		assertEquals(
			hemmingDistance(
				listOf(true, true, true, true),
				listOf(false, false, false, false)
			),
			4.0
		)
		assertEquals(
			hemmingDistance(
				listOf(true, true, true, true),
				listOf(true, true, true, true)
			),
			0.0
		)
		assertEquals(
			hemmingDistance(
				listOf(false, true, true, true),
				listOf(true, true, true, false)
			),
			2.0
		)
	}
}