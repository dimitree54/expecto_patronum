package we.rashchenko.patronum.ui.messages

import java.util.*


fun getLocalisedMessage(message: String, languageCode: String?): String {
    val locale = languageCode?.let{ Locale.forLanguageTag(languageCode) } ?: Locale.ENGLISH
    return ResourceBundle.getBundle("bot_messages", locale).getString(message)
}