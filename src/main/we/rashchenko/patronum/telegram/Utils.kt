package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import we.rashchenko.patronum.Wish
import we.rashchenko.patronum.getLocalisedMessage

fun getTelegramUser(update: Update): User? {
    return update.message?.from ?: update.callbackQuery?.from
}

fun getChatId(update: Update): ChatId.Id? {
    return update.message?.chat?.id?.let { ChatId.fromId(it) }
        ?: update.callbackQuery?.message?.chat?.id?.let { ChatId.fromId(it) }
}

fun sendWishCard(bot: Bot, user: User, chatId: ChatId.Id, wish: Wish) {
    bot.sendMessage(chatId = chatId, text = "**${wish.title}**\n${wish.description}" + (wish.location?.let {
        getLocalisedMessage(
            "request_confirmation", user.languageCode
        ).format(wish.radius, it)
    } ?: ""), parseMode = ParseMode.MARKDOWN)
}