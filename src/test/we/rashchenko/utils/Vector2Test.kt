package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.sqrt

internal class Vector2Test {
	private val eps = 0.00001f

	@Test
	fun assertEqual() {
		assertTrue(Vector2.ZERO == Vector2(0f, 0f))
		assertTrue(Vector2.ONES == Vector2(1f, 1f))
		assertTrue(Vector2(2f, 0.5f) == Vector2(2f, 0.5f))
	}

	@Test
	fun dst() {
		assertEquals(Vector2.ZERO.dst(Vector2.ZERO), 0f, eps)
		assertEquals(Vector2.ZERO.dst(Vector2.ONES), Vector2.ONES.dst(Vector2.ZERO), eps)
		assertEquals(Vector2(15f, 1f).dst(Vector2(15f, 2f)), 1f, eps)
	}

	@Test
	fun scl() {
		assertTrue(Vector2.ZERO.scl(Vector2.ONES) == Vector2.ZERO)
		assertTrue(Vector2.ONES.scl(Vector2.ZERO) == Vector2.ZERO)
		assertTrue(Vector2.ZERO.scl(Vector2.ZERO) == Vector2.ZERO)
		assertTrue(Vector2.ONES.scl(Vector2.ONES) == Vector2.ONES)
		assertTrue(Vector2(2f, -0.5f).scl(Vector2(0.5f, -2f)) == Vector2.ONES)
		assertTrue(Vector2(2f, 2f).scl(0.5f) == Vector2.ONES)
	}

	@Test
	fun len() {
		assertTrue(Vector2.ZERO.len() == 0f)
		assertTrue(Vector2.ONES.len() == sqrt(2f))
		assertTrue(Vector2.ONES.scl(-1f).len() == sqrt(2f))
		assertTrue(Vector2(0f, -1f).len() == 1f)
		assertTrue(Vector2(-0.5f, 0f).len() == 0.5f)
	}

	@Test
	fun normalize() {
		assertThrows<IllegalArgumentException> {
			Vector2.ZERO.normalize()
		}
		assertTrue(Vector2(2f, 0f).normalize() == Vector2(1f, 0f))
		assertTrue(Vector2(0f, -3f).normalize() == Vector2(0f, -1f))
		assertTrue(Vector2.ONES.normalize() == Vector2(1 / sqrt(2f), 1 / sqrt(2f)))
		assertTrue(Vector2.ONES.scl(-1f).normalize() == Vector2(-1 / sqrt(2f), -1 / sqrt(2f)))
	}
}