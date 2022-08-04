package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class NotFulfillingError: UserReadableError("NotFulfillingError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("menu_not_fulfilling", languageCode)
    }
}