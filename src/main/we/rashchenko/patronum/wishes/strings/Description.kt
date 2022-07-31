package we.rashchenko.patronum.wishes.strings

import java.util.*

class Description(text: String) : LimitedLengthString(text, Limits.minLength, Limits.maxLength) {
    private object Limits{
        val minLength: UInt
        val maxLength: UInt
        init{
            Properties().also {
                it.load(ClassLoader.getSystemResourceAsStream("limits.properties"))
                minLength = (it["minDescriptionLength"] as String).toUInt()
                maxLength = (it["maxDescriptionLength"] as String).toUInt()
            }
        }
    }
}