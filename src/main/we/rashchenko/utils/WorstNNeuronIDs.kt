package we.rashchenko.utils

import java.util.*


// @todo probably binary tree would be more efficient data structure for AlwaysSorted (as it does not require
//   shift a bunch of data on each add). But implementation would be much more massive.
open class AlwaysSorted<T>(private val comparator: Comparator<T>) : MutableCollection<T> {
	// @todo why we can not delegate interface implementation to val outside of primary constructor? Track status of
	//   that question here:
	//   https://stackoverflow.com/questions/33966186/how-to-delegate-implementation-to-a-property-in-kotlin
	private val data = mutableListOf<T>()
	override val size: Int
		get() = data.size

	override fun isEmpty(): Boolean = data.isEmpty()
	override fun iterator(): MutableIterator<T> = data.iterator()
	override fun clear() = data.clear()
	override fun retainAll(elements: Collection<T>): Boolean = data.retainAll(elements)

	override fun contains(element: T): Boolean {
		val index = Collections.binarySearch(data, element, comparator)
		return index >= 0
	}

	override fun containsAll(elements: Collection<T>): Boolean = elements.map { contains(it) }.all { it }

	override fun remove(element: T): Boolean {
		val index = Collections.binarySearch(data, element, comparator)
		data.removeAt(index)
		return index >= 0
	}

	override fun removeAll(elements: Collection<T>): Boolean = elements.map { remove(it) }.any()

	override fun add(element: T): Boolean {
		var index = Collections.binarySearch(data, element, comparator)
		if (index < 0) index = index.inv()
		data.add(index, element)
		return true
	}

	override fun addAll(elements: Collection<T>): Boolean = elements.map { add(it) }.any()

	// using the same syntax as in TreeSet:
	fun comparator(): Comparator<T> = comparator
	fun pollFirst() {
		data.removeAt(0)
	}
}

/**
AlwaysSorted Collection with limited capacity. If there is no
vacant space left, and you are trying to add more it removes the least element.
 */
open class BestN<T>(private val n: Int, comparator: Comparator<T>) : AlwaysSorted<T>(comparator) {
	override fun add(element: T): Boolean {
		return if (size + 1 > n) {
			if (comparator().compare(this.first(), element) < 0) {
				pollFirst()
				super.add(element)
			} else {
				false
			}
		} else {
			super.add(element)
		}
	}
}

class NeuronsWithFeedbackComparator : Comparator<Pair<Int, Feedback>> {
	override fun compare(o1: Pair<Int, Feedback>, o2: Pair<Int, Feedback>): Int =
		o1.second.compareTo(o2.second)
}

class InvertedNeuronsWithFeedbackComparator : Comparator<Pair<Int, Feedback>> {
	private val baseComparator = NeuronsWithFeedbackComparator()
	override fun compare(o1: Pair<Int, Feedback>, o2: Pair<Int, Feedback>): Int {
		return -baseComparator.compare(o1, o2)
	}
}

class WorstNNeuronIDs(n: Int) : BestN<Pair<Int, Feedback>>(n, InvertedNeuronsWithFeedbackComparator())
