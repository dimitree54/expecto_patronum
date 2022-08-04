package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class WishClosedError(): UserReadableError("WishClosedError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("hotel_room_closed", languageCode)
    }
}