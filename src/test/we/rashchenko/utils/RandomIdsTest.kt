package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RandomIdsTest {

	@Test
	fun testForSetProperties() {
		val dataSize = 1000000
		val data = mutableSetOf<Int>()
		repeat(dataSize) {
			data.add(randomIds.next())
		}
		assertEquals(data.size, dataSize)
	}
}