package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BestNTest {

	@Test
	fun testForDuplicatesSimpleData() {
		val bestN = BestN<Int>(5) { o1, o2 -> o1.compareTo(o2) }
		assertEquals(bestN.size, 0)
		bestN.add(5)
		assertEquals(bestN.size, 1)
		bestN.add(5)
		assertEquals(bestN.size, 2)
	}

	@Test
	fun testForDuplicatesComplexData() {
		val bestN = BestN<Pair<Int, Int>>(5) { o1, o2 -> o1.second.compareTo(o2.second) }
		assertEquals(bestN.size, 0)
		bestN.add(Pair(1, 5))
		assertEquals(bestN.size, 1)
		bestN.add(Pair(2, 5))
		assertEquals(bestN.size, 2)
	}

	@Test
	fun testForLimitedCapacity() {
		val bestN = BestN<Int>(3) { o1, o2 -> o1.compareTo(o2) }
		assertEquals(bestN.size, 0)
		bestN.addAll(listOf(2, 3, 4))
		assertEquals(bestN.size, 3)
		bestN.add(1)
		assertEquals(bestN.size, 3)
		bestN.add(3)
		assertEquals(bestN.size, 3)
		bestN.add(5)
		assertEquals(bestN.size, 3)
	}

	@Test
	fun testForBestStoring() {
		val bestN = BestN<Int>(3) { o1, o2 -> o1.compareTo(o2) }
		assertFalse(bestN.contains(2))
		bestN.addAll(listOf(2, 3, 5))
		assertTrue(bestN.contains(2))
		assertFalse(bestN.add(1))
		assertFalse(bestN.contains(1))
		assertTrue(bestN.add(4))
		assertTrue(bestN.contains(4))
		assertFalse(bestN.contains(2))
	}
}