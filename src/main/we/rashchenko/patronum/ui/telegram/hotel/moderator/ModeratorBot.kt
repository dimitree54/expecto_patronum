package we.rashchenko.patronum.ui.telegram.hotel.moderator

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.database.stats.Report
import we.rashchenko.patronum.ui.telegram.bot.sendMessageMultiLanguage
import we.rashchenko.patronum.ui.telegram.bot.sendWishCard

class ModeratorBot(private val database: Database) {
    fun build() = bot {
        token = System.getenv("TELEGRAM_EP_OBSERVER_BOT_TOKEN")
        timeout = 30
        dispatch {
            command(ModeratorCommand.START.command) {
                val chatId = message.chat.id
                database.getRoomByTelegramId(chatId)?.also {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_greetings", warn = true)
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_wish_intro", warn = false)
                    sendWishCard(bot, ChatId.fromId(chatId), it.wish, it.getLanguageCodes())
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
            command(ModeratorCommand.FINISH.command) {
                val chatId = message.chat.id
                val senderId = message.from!!.id
                database.getRoomByTelegramId(chatId) ?. also{
                    if (it.closed) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_room_closed", warn = false)
                    } else {
                        if (senderId == it.wish.author.telegramId) {
                            bot.sendMessageMultiLanguage(
                                ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_wish_finish_by_author", warn = false
                            )
                            database.finishWish(it.wish)
                        } else {
                            bot.sendMessageMultiLanguage(
                                ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_wish_finish_by_patron", warn = false
                            )
                        }
                    }
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
            command(ModeratorCommand.CANCEL.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                database.getRoomByTelegramId(chatId) ?. also{
                    if (it.closed) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_room_closed", warn = false)
                    } else {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_wish_cancel", warn = false)
                        if (senderId == it.wish.author.telegramId) {
                            database.cancelWishByAuthor(it.wish)
                        } else {
                            database.cancelWishByPatron(it.wish)
                        }
                    }
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
            command(ModeratorCommand.REPORT.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                database.getRoomByTelegramId(chatId) ?.also{
                    if (!it.finished && !it.canceledByAuthor && !it.canceledByPatron) {
                        if (senderId == it.wish.author.telegramId && !it.reportedByAuthor) {
                            bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_report", warn = false)
                            val report = Report(database.generateNewReportId(), it.wish.author, it.wish.patron!!, "")
                            database.registerReport(report)
                        } else if (senderId == it.wish.patron!!.telegramId && !it.reportedByPatron) {
                            bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "hotel_report", warn = false)
                            val report = Report(database.generateNewReportId(), it.wish.patron!!, it.wish.author, "")
                            database.registerReport(report)
                        }
                    }
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
        }
    }
}