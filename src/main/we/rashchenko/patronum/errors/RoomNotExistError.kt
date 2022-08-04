package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class RoomNotExistError: UserReadableError("RoomNotExistError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("hotel_room_unknown", languageCode)
    }
}