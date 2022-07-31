package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class TooShortError(private val length: UInt, private val limit: UInt) :
    UserReadableError("Too short ($length < $limit)") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("make_wish_error_short", languageCode).format(length.toInt(), limit.toInt())
    }
}