package we.rashchenko.patronum.wishes.strings

open class LimitedLengthString(val value: String, minLength: UInt, maxLength: UInt) {
    init {
        if (value.length.toUInt() !in minLength..maxLength) {
            throw BadLengthError()
        }
    }
}