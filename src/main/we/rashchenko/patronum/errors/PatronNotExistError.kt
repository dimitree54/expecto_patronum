package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class PatronNotExistError: UserReadableError("PatronNotExistError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("database_patron_absent", languageCode)
    }
}