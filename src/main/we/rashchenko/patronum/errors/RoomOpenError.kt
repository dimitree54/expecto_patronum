package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class RoomOpenError(errorMessage: String): UserReadableError(errorMessage) {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("room_open_error", languageCode)
    }
}