package we.rashchenko.patronum.hotel

class WishRoom(
    val id: String,
    val telegramChatId: Long,
    val wishId: String,
    val authorId: String,
    val patronId: String
) {
    private var languageCodes = mutableSetOf<String>()
    var reportedByAuthor = false
    var reportedByPatron = false

    fun addLanguageCode(languageCode: String) {
        languageCodes.add(languageCode)
    }
    fun getLanguageCodes(): Set<String> {
        return languageCodes.toSet()
    }
}