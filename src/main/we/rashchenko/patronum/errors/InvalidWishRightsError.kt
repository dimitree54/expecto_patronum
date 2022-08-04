package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class InvalidWishRightsError: UserReadableError("InvalidWishRightsError") {
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("hotel_wish_rights_invalid", languageCode)
    }
}