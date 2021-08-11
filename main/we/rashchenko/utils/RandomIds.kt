package we.rashchenko.utils

import java.util.*

val randomIds = sequence {
	val existingIds = mutableSetOf<Int>()
	val random = Random()
	while (true) {
		val randomId = random.nextInt()
		if (randomId !in existingIds) {
			existingIds.add(randomId)
			yield(randomId)
		}
	}
}.iterator()