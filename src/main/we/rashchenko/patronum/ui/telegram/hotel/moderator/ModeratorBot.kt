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
                val room = database.getRoomByTelegramId(chatId)!!
                bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_greetings")
                bot.sendCardMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), room.wish)
            }
            command(ModeratorCommand.FINISH.command) {
                val chatId = message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)!!
                if (room.closed) {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_closed")
                } else {
                    if (senderId == room.wish.author.telegramId) {
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "room_finish_by_author"
                        )
                        database.finishWish(room.wish)
                    } else {
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "room_finish_by_patron"
                        )
                    }
                }
            }
            command(ModeratorCommand.CANCEL.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)!!
                if (room.closed) {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_closed")
                } else {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_cancel")
                    if (senderId == room.wish.author.telegramId) {
                        database.cancelWishByAuthor(room.wish)
                    } else {
                        database.cancelWishByPatron(room.wish)
                    }
                }
            }
            command(ModeratorCommand.REPORT.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)!!

                if (!room.finished && !room.canceledByAuthor && !room.canceledByPatron) {
                    if (senderId == room.wish.author.telegramId && !room.reportedByAuthor) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_report")
                        val report = Report(database.generateNewReportId(), room.wish.author, room.wish.patron!!, "")
                        database.registerReport(report)
                    } else if(senderId == room.wish.patron!!.telegramId && !room.reportedByPatron) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "room_report")
                        val report = Report(database.generateNewReportId(), room.wish.patron!!, room.wish.author, "")
                        database.registerReport(report)
                    }
                }
            }
        }
    }
}