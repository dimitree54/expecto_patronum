package we.rashchenko.utils.collections

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class WorstNTest {

	@Test
	fun testForDuplicatesSimpleData() {
		val worstN = WorstN<Int>(5) { o1, o2 -> o1.compareTo(o2) }
		assertEquals(worstN.size, 0)
		worstN.add(5)
		assertEquals(worstN.size, 1)
		worstN.add(5)
		assertEquals(worstN.size, 2)
	}

	@Test
	fun testForDuplicatesComplexData() {
		val worstN = WorstN<Pair<Int, Int>>(5) { o1, o2 -> o1.second.compareTo(o2.second) }
		assertEquals(worstN.size, 0)
		worstN.add(Pair(1, 5))
		assertEquals(worstN.size, 1)
		worstN.add(Pair(2, 5))
		assertEquals(worstN.size, 2)
		worstN.add(Pair(2, 5))
		assertEquals(worstN.size, 3)
	}

	@Test
	fun testForLimitedCapacity() {
		val worstN = WorstN<Int>(3) { o1, o2 -> o1.compareTo(o2) }
		assertEquals(worstN.size, 0)
		worstN.addAll(listOf(2, 3, 4))
		assertEquals(worstN.size, 3)
		worstN.add(1)
		assertEquals(worstN.size, 3)
		worstN.add(3)
		assertEquals(worstN.size, 3)
		worstN.add(5)
		assertEquals(worstN.size, 3)
	}

	@Test
	fun testForWorstStoring() {
		val worstN = WorstN<Int>(3) { o1, o2 -> o1.compareTo(o2) }
		assertFalse(worstN.contains(2))
		worstN.addAll(listOf(2, 3, 5))
		assertTrue(worstN.contains(2))
		assertFalse(worstN.add(6))
		assertFalse(worstN.contains(6))
		assertTrue(worstN.add(4))
		assertTrue(worstN.contains(4))
		assertFalse(worstN.contains(5))
	}
}