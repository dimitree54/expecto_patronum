package we.rashchenko.patronum.ui.messages

import java.util.*


fun getLocalisedMessage(message: String, languageCode: String?): String {
    return ResourceBundle.getBundle("bot_messages", Locale.forLanguageTag(languageCode)).getString(message)
}