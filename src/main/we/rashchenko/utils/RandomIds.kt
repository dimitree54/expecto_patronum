package we.rashchenko.utils

/**
 * Sequence of integers without repetition. For now, it is consecutive.
 */
class IDsGenerator{
    private var lastID = 0
    fun next(): Int{
        return lastID++
    }
}
