package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class TooLongError(private val length: UInt, private val limit: UInt) :
    UserReadableError("Too long ($length > $limit)") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("string_too_long", languageCode).format(length.toInt(), limit.toInt())
    }
}