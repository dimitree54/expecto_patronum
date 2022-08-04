package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class AlreadyFulfillingError: UserReadableError("AlreadyFulfillingError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("menu_already_fulfilling", languageCode)
    }
}