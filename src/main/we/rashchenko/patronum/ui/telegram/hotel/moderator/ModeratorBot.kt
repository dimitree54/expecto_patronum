package we.rashchenko.patronum.ui.telegram.hotel.moderator

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.database.stats.Report
import we.rashchenko.patronum.ui.telegram.bot.sendCardMultiLanguage
import we.rashchenko.patronum.ui.telegram.bot.sendMessageMultiLanguage

class ModeratorBot(private val database: Database) {
    fun build() = bot {
        token = System.getenv("TELEGRAM_EP_OBSERVER_BOT_TOKEN")
        timeout = 30
        dispatch {
            command(ModeratorCommand.START.command) {
                val chatId = message.chat.id
                database.getRoomByTelegramId(chatId)?.also {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_greetings")
                    bot.sendCardMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), it.wish)
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
            command(ModeratorCommand.FINISH.command) {
                val chatId = message.chat.id
                val senderId = message.from!!.id
                database.getRoomByTelegramId(chatId) ?. also{
                    if (it.closed) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_closed")
                    } else {
                        if (senderId == it.wish.author.telegramId) {
                            bot.sendMessageMultiLanguage(
                                ChatId.fromId(chatId), it.getLanguageCodes(), "room_finish_by_author"
                            )
                            database.finishWish(it.wish)
                        } else {
                            bot.sendMessageMultiLanguage(
                                ChatId.fromId(chatId), it.getLanguageCodes(), "room_finish_by_patron"
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
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_closed")
                    } else {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_cancel")
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
                            bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_report")
                            val report = Report(database.generateNewReportId(), it.wish.author, it.wish.patron!!, "")
                            database.registerReport(report)
                        } else if (senderId == it.wish.patron!!.telegramId && !it.reportedByPatron) {
                            bot.sendMessageMultiLanguage(ChatId.fromId(chatId), it.getLanguageCodes(), "room_report")
                            val report = Report(database.generateNewReportId(), it.wish.patron!!, it.wish.author, "")
                            database.registerReport(report)
                        }
                    }
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
        }
    }
}