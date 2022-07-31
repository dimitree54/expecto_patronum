package we.rashchenko.patronum.wishes.strings

import java.util.*

class Title(text: String) : LimitedLengthString(text, Limits.minLength, Limits.maxLength) {
    private object Limits{
        val minLength: UInt
        val maxLength: UInt
        init{
            Properties().also {
                it.load(ClassLoader.getSystemResourceAsStream("limits.properties"))
                minLength = (it["minTitleLength"] as String).toUInt()
                maxLength = (it["maxTitleLength"] as String).toUInt()
            }
        }
    }
}