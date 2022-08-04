package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class UserNotExistError: UserReadableError("UserNotExistError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("database_user_unknown", languageCode)
    }
}