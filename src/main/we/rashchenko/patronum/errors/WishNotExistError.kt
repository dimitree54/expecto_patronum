package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class WishNotExistError: UserReadableError("WishNotExistError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("database_wish_unknown", languageCode)
    }
}