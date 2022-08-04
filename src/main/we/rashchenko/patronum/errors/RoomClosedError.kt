package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class RoomClosedError: UserReadableError("RoomClosedError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("hotel_room_closed", languageCode)
    }
}