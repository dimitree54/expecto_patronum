package we.rashchenko.utils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KNearestVectorsConnectionSamplerTest {

	@Test
	fun testConnect() {
		val v01 = Vector2(0f, 1f)
		val v10 = Vector2(1f, 0f)
		val allVectors = mutableListOf(Vector2.ZERO, Vector2.ONES, v01, v10)
		KNearestVectorsConnectionSampler(2).also { sampler ->
			sampler.connectAll(allVectors).also {
				assertTrue(it[Vector2.ZERO]!!.toSet() == setOf(v01, v10))
				assertTrue(it[Vector2.ONES]!!.toSet() == setOf(v01, v10))
				assertTrue(it[v01]!!.toSet() == setOf(Vector2.ZERO, Vector2.ONES))
				assertTrue(it[v10]!!.toSet() == setOf(Vector2.ZERO, Vector2.ONES))
			}
			allVectors.add(Vector2(0f, 0.5f))
			Vector2(0f, 0.5f).also { position ->
				sampler.connectNew(position, allVectors).also {
					assertTrue(it.toSet() == setOf(v01, Vector2.ZERO))
				}
			}
		}
	}

	@Test
	fun testInvalidNewPosition() {
		val v01 = Vector2(0f, 1f)
		val v10 = Vector2(1f, 0f)
		val allVectors = listOf(Vector2.ZERO, Vector2.ONES, v01, v10)
		KNearestVectorsConnectionSampler(2).also { sampler ->
			assertThrows<IllegalArgumentException> {
				sampler.connectNew(Vector2(0f, 0.5f), allVectors)
			}
		}
	}

	@Test
	fun testBigK() {
		val v01 = Vector2(0f, 1f)
		val v10 = Vector2(1f, 0f)
		val allVectors = mutableListOf(Vector2.ZERO, Vector2.ONES, v01, v10)
		KNearestVectorsConnectionSampler(100).also { sampler ->
			sampler.connectAll(allVectors).also {
				assertTrue(it[Vector2.ZERO]!!.toSet() == setOf(v01, v10, Vector2.ONES))
				assertTrue(it[Vector2.ONES]!!.toSet() == setOf(v01, v10, Vector2.ZERO))
				assertTrue(it[v01]!!.toSet() == setOf(Vector2.ZERO, Vector2.ONES, v10))
				assertTrue(it[v10]!!.toSet() == setOf(Vector2.ZERO, Vector2.ONES, v01))
			}
			allVectors.add(Vector2(0f, 0.5f))
			Vector2(0f, 0.5f).also { position ->
				sampler.connectNew(position, allVectors).also {
					assertTrue(it.toSet() == setOf(Vector2.ZERO, v01, v10, Vector2.ONES))
				}
			}
		}
	}
}