package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import we.rashchenko.patronum.UserStatistics
import we.rashchenko.patronum.Wish
import we.rashchenko.patronum.getLocalisedMessage

fun getTelegramUser(update: Update): User? {
    return update.message?.from ?: update.callbackQuery?.from
}

fun getChatId(update: Update): ChatId.Id? {
    return update.message?.chat?.id?.let { ChatId.fromId(it) }
        ?: update.callbackQuery?.message?.chat?.id?.let { ChatId.fromId(it) }
}

fun askAndWaitForAnswer(
    answer: Message?, sendRequestMessage: () -> Unit, checkValidText: (Message?) -> Boolean
): Message? {
    return if (checkValidText(answer)) {
        answer
    } else {
        sendRequestMessage()
        null
    }
}

fun sendWishCard(bot: Bot, user: User, chatId: ChatId.Id, wish: Wish) {
    bot.sendMessage(chatId = chatId, text = "**${wish.title}**\n${wish.description}" + (wish.location?.let {
        getLocalisedMessage(
            "location_format", user.languageCode
        ).format(wish.radius, it)
    } ?: ""), parseMode = ParseMode.MARKDOWN)
}

fun sendUserStatistics(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStatistics) {
    bot.sendMessage(chatId = chatId, text = getLocalisedMessage("user_statistics_format", user.languageCode).format(
        stats.numActiveWishes, stats.numFulfilledUserWishes, stats.numFulfilledByUserWishes, stats.score, stats.rating
    ))
}