package we.rashchenko.patronum.wishes.strings

import we.rashchenko.patronum.errors.TooLongError
import we.rashchenko.patronum.errors.TooShortError

open class LimitedLengthString(val text: String, minLength: UInt, maxLength: UInt) {
    init {
        if (text.length.toUInt() < minLength) {
            throw TooShortError(text.length.toUInt(), minLength)
        } else if (text.length.toUInt() > maxLength) {
            throw TooLongError(text.length.toUInt(), maxLength)
        }
    }
}