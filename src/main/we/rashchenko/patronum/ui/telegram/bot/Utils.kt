package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import we.rashchenko.patronum.database.stats.GlobalStats
import we.rashchenko.patronum.database.stats.UserStats
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

fun Wish.formatToString(languageCode: String?): String {
    return "*${title.text.replaceMarkdownSpecialSymbols()}*\n\n${description.text.replaceMarkdownSpecialSymbols()}\n\n${
        searchInfo.formatToString(
            languageCode
        )
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
    searchInfoDraft?.toSearchInfo()?.let { text.append(it.formatToString(languageCode)) } ?: return text.toString()
    return text.toString()
}

fun SearchInfo.formatToString(languageCode: String?): String {
    return if (this.searchArea != null) {
        "_${getLocalisedMessage("location_provided", languageCode).replaceMarkdownSpecialSymbols()}_"
    } else {
        "_${getLocalisedMessage("location_absent", languageCode).replaceMarkdownSpecialSymbols()}_"
    }
}

fun sendWishCard(bot: Bot, user: User, chatId: ChatId.Id, wish: Wish) {
    bot.sendMessage(
        chatId = chatId, text = wish.formatToString(user.languageCode), parseMode = ParseMode.MARKDOWN_V2
    )
}

fun sendWishDraftCard(bot: Bot, user: User, chatId: ChatId.Id, wishDraft: WishDraft) {
    bot.sendMessage(
        chatId = chatId, text = wishDraft.formatToString(user.languageCode), parseMode = ParseMode.MARKDOWN_V2
    )
}

fun sendUserStatistics(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStats, globalStats: GlobalStats) {
    bot.sendMessage(
        chatId = chatId, text = getLocalisedMessage("user_statistics_format", user.languageCode).format(
            stats.myWishesActive,
            stats.myWishesDone,
            stats.othersWishesDone,
            stats.reputation.roundToInt(),
            (globalStats.getRateOnLeaderBoard(stats.reputation) * 100).roundToInt()
        )
    )
}

fun Bot.sendMessageMultiLanguage(
    chatId: ChatId,
    setOfLanguages: Set<String>,
    messageName: String,
    warn: Boolean = true,
) {
    if (setOfLanguages.size > 1 && warn) {
        sendMessageMultiLanguage(chatId, setOfLanguages, "room_translator_required", false)
    }
    setOfLanguages.ifEmpty { setOf(null) }.forEach {
        sendMessage(chatId, getLocalisedMessage(messageName, it))
    }
}

fun Bot.sendCardMultiLanguage(chatId: ChatId, setOfLanguages: Set<String>, wish: Wish) {
    setOfLanguages.ifEmpty { setOf(null) }.forEach {
        sendMessage(chatId, wish.formatToString(it))
    }
}

fun Bot.clearKeyboard(chatId: ChatId, message: String) {
    sendMessage(
        chatId,
        message,
        replyMarkup = ReplyKeyboardRemove()
    )
}
