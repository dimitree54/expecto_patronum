package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import we.rashchenko.patronum.Wish

fun establishContact(bot: Bot, chatId: ChatId.Id, telegramPatronId: Long, wish: Wish) {
    // todo create chat with bot as moderator (not supported by Bot API, but can be done by command line)
    bot.sendMessage(chatId, text="")
}