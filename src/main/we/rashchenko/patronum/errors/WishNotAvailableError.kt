package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class WishNotAvailableError: UserReadableError("WishNotAvailableError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("database_wish_taken", languageCode)
    }
}