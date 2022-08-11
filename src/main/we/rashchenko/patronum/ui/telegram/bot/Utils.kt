package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import we.rashchenko.patronum.database.stats.GlobalStats
import we.rashchenko.patronum.database.stats.UserStats
import we.rashchenko.patronum.errors.UserReadableError
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.wishes.WishDraft
import kotlin.math.roundToInt

fun getTelegramUser(update: Update): User? {
    return update.message?.from ?: update.callbackQuery?.from
}

fun getChatId(update: Update): ChatId.Id? {
    return update.message?.chat?.id?.let { ChatId.fromId(it) }
        ?: update.callbackQuery?.message?.chat?.id?.let { ChatId.fromId(it) }
}

fun askAndWaitForAnswer(
    answer: Message?, sendRequestMessage: () -> Unit, checkValidText: (Message?) -> Boolean,
): Message? {
    return if (checkValidText(answer)) {
        answer
    } else {
        sendRequestMessage()
        null
    }
}

fun Wish.formatToStringMultiLanguage(languageCodes: Set<String?>): String {
    return "*${
        title.text.replaceMarkdownSpecialSymbols()}*\n\n${
            description.text.replaceMarkdownSpecialSymbols()
        }\n\n${
                searchInfo.formatToStringMultiLanguage(languageCodes)
        }"
}

fun String.replaceMarkdownSpecialSymbols(): String {
    val specialMarkdownCharacters = listOf("_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!")
    return specialMarkdownCharacters.fold(this) { acc, specialCharacter ->
        acc.replace(specialCharacter, "\\$specialCharacter")
    }
}

fun WishDraft.formatToString(languageCode: String?): String {
    val text = StringBuilder()
    title?.let { text.append("*${it.text.replaceMarkdownSpecialSymbols()}*\n\n") } ?: return text.toString()
    description?.let { text.append("${it.text.replaceMarkdownSpecialSymbols()}\n\n") } ?: return text.toString()
    searchInfoDraft?.toSearchInfo()?.let { text.append(it.formatToStringMultiLanguage(setOf(languageCode))) } ?: return text.toString()
    return text.toString()
}

fun SearchInfo.formatToStringMultiLanguage(languageCodes: Set<String?>): String {
    val text = StringBuilder()
    languageCodes.ifEmpty { setOf(null) }.forEach {
        if (this.searchArea != null) {
            text.append("_${getLocalisedMessage("browser_location_provided", it).replaceMarkdownSpecialSymbols()}_\n\n")
        } else {
            text.append("_${getLocalisedMessage("browser_location_absent", it).replaceMarkdownSpecialSymbols()}_\n\n")
        }
    }
    return text.toString()
}

fun sendWishCard(bot: Bot, chatId: ChatId.Id, wish: Wish, languageCodes: Set<String?>) {
    bot.sendMessage(
        chatId = chatId, text = wish.formatToStringMultiLanguage(languageCodes), parseMode = ParseMode.MARKDOWN_V2
    )
}

fun sendWishDraftCard(bot: Bot, user: User, chatId: ChatId.Id, wishDraft: WishDraft) {
    bot.sendMessage(
        chatId = chatId, text = wishDraft.formatToString(user.languageCode), parseMode = ParseMode.MARKDOWN_V2
    )
}

fun sendUserStatistics(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStats, globalStats: GlobalStats) {
    bot.sendMessage(
        chatId = chatId, text = getLocalisedMessage("menu_stats", user.languageCode).format(
            stats.myWishesActive,
            stats.myWishesDone,
            stats.othersWishesDone,
            stats.reputation,
            (globalStats.getRateOnLeaderBoard(stats.reputation) * 100).roundToInt()
        )
    )
}

fun Bot.warnAboutMultiLanguage(
    chatId: ChatId,
    setOfLanguages: Set<String>
){
    sendMessageMultiLanguage(chatId, setOfLanguages, "hotel_warning_multilanguage")
}

fun Bot.sendMessageMultiLanguage(
    chatId: ChatId,
    setOfLanguages: Set<String>,
    messageName: String
) {
    setOfLanguages.ifEmpty { setOf(null) }.forEach {
        sendMessage(chatId, getLocalisedMessage(messageName, it))
    }
}

fun Bot.sendErrorMultiLanguage(
    chatId: ChatId,
    setOfLanguages: Set<String>,
    error: UserReadableError
){
    setOfLanguages.ifEmpty { setOf(null) }.forEach {
        sendMessage(chatId, error.getUserReadableMessage(it))
    }
}

fun Bot.clearKeyboard(chatId: ChatId, message: String) {
    sendMessage(
        chatId,
        message,
        replyMarkup = ReplyKeyboardRemove()
    )
}
