package we.rashchenko.patronum.ui.telegram.hotel.moderator

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.entities.ChatId
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.ui.telegram.bot.sendMessageMultiLanguage
import we.rashchenko.patronum.ui.telegram.bot.sendWishCard
import we.rashchenko.patronum.ui.telegram.bot.warnAboutMultiLanguage

class ModeratorBot(private val database: Database) {
    private fun Dispatcher.safeRoomCommand(
        command: String,
        handleRoomCommand: (Bot, Long, ChatId.Id, WishRoom) -> Unit,
    ) {
        addHandler(SafeRoomCommandHandler(database, command, handleRoomCommand))
    }

    fun build() = bot {
        token = System.getenv("TELEGRAM_EP_OBSERVER_BOT_TOKEN")
        timeout = 30
        dispatch {
            safeRoomCommand(ModeratorCommand.START.command) { bot, _, chatId, room ->
                val languages = room.getLanguageCodes()
                if (languages.size > 1) {
                    bot.warnAboutMultiLanguage(chatId, languages)
                }
                val wish = database.getWishById(room.wishId)
                bot.sendMessageMultiLanguage(chatId, languages, "hotel_greetings")
                bot.sendMessageMultiLanguage(chatId, languages, "hotel_wish_intro")
                sendWishCard(bot, chatId, wish, languages)
            }
            safeRoomCommand(ModeratorCommand.FINISH.command){ bot, senderId, chatId, room ->
                database.finishRoomWish(senderId, room)
                bot.sendMessageMultiLanguage(chatId, room.getLanguageCodes(), "hotel_wish_finish_by_author")
            }
            safeRoomCommand(ModeratorCommand.CANCEL.command){ bot, senderId, chatId, room ->
                database.cancelRoomWish(senderId, room)
                bot.sendMessageMultiLanguage(chatId, room.getLanguageCodes(), "hotel_wish_cancel")

            }
            safeRoomCommand(ModeratorCommand.REPORT.command){ bot, senderId, chatId, room ->
                database.registerRoomReport(senderId, room)
                bot.sendMessageMultiLanguage(chatId, room.getLanguageCodes(), "hotel_report")
            }
        }
    }
}