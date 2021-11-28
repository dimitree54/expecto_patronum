package we.rashchenko.utils.collections

import java.util.*

/**
 * Always sorted list. It compares values based on provided [comparator].
 */
open class AlwaysSorted<T>(val comparator: Comparator<T>) : MutableList<T> {
    private val data = mutableListOf<T>()
    override val size: Int
        get() = data.size

    override fun isEmpty(): Boolean = data.isEmpty()
    override fun iterator(): MutableIterator<T> = data.iterator()
    override fun clear() = data.clear()
    override fun retainAll(elements: Collection<T>): Boolean = data.retainAll(elements.toSet())
    override fun addAll(index: Int, elements: Collection<T>): Boolean = addAll(elements)
    override fun listIterator(): MutableListIterator<T> = data.listIterator()
    override fun listIterator(index: Int): MutableListIterator<T> = data.listIterator(index)
    override fun removeAt(index: Int): T = data.removeAt(index)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = data.subList(fromIndex, toIndex)
    override fun get(index: Int): T = data[index]

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

    override fun indexOf(element: T): Int {
        var index = Collections.binarySearch(data, element, comparator)
        if (index < 0) return -1
        else {
            while (true) {
                // ensuring that we return the first of the relevant elements
                if (index == 0 || comparator.compare(data[index - 1], element) != 0) {
                    return index
                } else {
                    index--
                }
            }
        }
    }

    override fun lastIndexOf(element: T): Int {
        var index = Collections.binarySearch(data, element, comparator)
        if (index < 0) return -1
        else {
            while (true) {
                // ensuring that we return the last of the relevant elements
                if (index == size - 1 || comparator.compare(data[index + 1], element) != 0) {
                    return index
                } else {
                    index++
                }
            }
        }
    }

    override fun add(index: Int, element: T) {
        add(element)
    }

    override fun set(index: Int, element: T): T {
        val replaced = data[index]
        removeAt(index)
        add(element)
        return replaced
    }

}