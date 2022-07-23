package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import we.rashchenko.patronum.database.stats.GlobalStats
import we.rashchenko.patronum.database.stats.UserStats
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.search.SearchInfoDraft
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.wishes.WishDraft

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
    return "**${title}**\n${description}\n\n${searchInfo.formatToString(languageCode)}"
}

fun WishDraft.formatToString(languageCode: String?): String {
    val text = StringBuilder()
    title?.let { text.append("**${it}**\n") } ?: return text.toString()
    description?.let { text.append("${it}\n\n") } ?: return text.toString()
    searchInfoDraft?.let { text.append(it.formatToString(languageCode)) } ?: return text.toString()
    return text.toString()
}

fun SearchInfo.formatToString(languageCode: String?): String {
    return if (this.searchArea != null) {
        "<i>${getLocalisedMessage("location_provided", languageCode)}</i>"
    } else {
        "<i>${getLocalisedMessage("location_absent", languageCode)}</i>"
    }
}

fun SearchInfoDraft.formatToString(languageCode: String?): String {
    return if (this.location != null && this.radius != null) {
        "<i>${getLocalisedMessage("location_provided", languageCode)}</i>"
    } else {
        "<i>${getLocalisedMessage("location_absent", languageCode)}</i>"
    }
}

fun sendWishCard(bot: Bot, user: User, chatId: ChatId.Id, wish: Wish) {
    bot.sendMessage(
        chatId = chatId, text = wish.formatToString(user.languageCode), parseMode = ParseMode.MARKDOWN
    )
}

fun sendWishDraftCard(bot: Bot, user: User, chatId: ChatId.Id, wishDraft: WishDraft) {
    bot.sendMessage(
        chatId = chatId, text = wishDraft.formatToString(user.languageCode), parseMode = ParseMode.MARKDOWN
    )
}

fun sendUserStatistics(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStats, globalStats: GlobalStats) {
    bot.sendMessage(
        chatId = chatId, text = getLocalisedMessage("user_statistics_format", user.languageCode).format(
            stats.myWishesActive,
            stats.myWishesDone,
            stats.othersWishesDone,
            stats.reputation,
            globalStats.getRateOnLeaderBoard(stats.reputation)
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
