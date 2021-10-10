package we.rashchenko.utils.collections

/**
AlwaysSorted Collection with limited capacity. If there is no
vacant space left, and you are trying to add more it removes the least element.
 */
open class WorstN<T>(private val n: Int, comparator: Comparator<T>) : AlwaysSorted<T>(comparator) {
    override fun add(element: T): Boolean {
        return if (size + 1 > n) {
            if (comparator.compare(this.last(), element) > 0) {
                removeAt(size - 1)
                super.add(element)
            } else {
                false
            }
        } else {
            super.add(element)
        }
    }
}