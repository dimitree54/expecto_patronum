package we.rashchenko.patronum.errors

import we.rashchenko.patronum.ui.messages.getLocalisedMessage

class WishDraftNotFilledError: UserReadableError("WishDraftNotFilledError"){
    override fun getUserReadableMessage(languageCode: String?): String {
        return getLocalisedMessage("database_wish_draft_invalid", languageCode)
    }

}