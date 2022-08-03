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
                    sendWishCard(bot, ChatId.fromId(chatId), it.wish!!, it.getLanguageCodes())
                } ?: bot.sendMessage(ChatId.fromId(chatId), "Unknown room")
            }
            command(ModeratorCommand.FINISH.command) {
                val chatId = message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)
                if (room == null){
                    bot.sendMessageMultiLanguage(
                        ChatId.fromId(chatId), setOf(), "hotel_room_unknown", warn = false
                    )
                }
                else if (room.closed) {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_room_closed", warn = false)
                } else {
                    val wish = room.wish
                    if (wish == null){
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_unknown", warn = false
                        )
                    }
                    else if (senderId == wish.author.telegramId) {
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_finish_by_author", warn = false
                        )
                        room.finished = true
                        database.updateRoom(room)
                        database.finishWish(wish)
                    } else {
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_finish_by_patron", warn = false
                        )
                    }
                }
            }
            command(ModeratorCommand.CANCEL.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)
                if (room == null){
                    bot.sendMessageMultiLanguage(
                        ChatId.fromId(chatId), setOf(), "hotel_room_unknown", warn = false
                    )
                }
                else if (room.closed) {
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_room_closed", warn = false)
                } else {
                    val wish = room.wish
                    if (wish == null){
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_unknown", warn = false
                        )
                    }
                    else{
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_cancel", warn = false)
                        if (senderId == wish.author.telegramId) {
                            room.canceledByAuthor = true
                            database.cancelWishByAuthor(wish)
                        } else {
                            room.canceledByPatron = true
                            database.cancelWishByPatron(wish)
                        }
                        database.updateRoom(room)
                    }
                }
            }
            command(ModeratorCommand.REPORT.command) {
                val chatId = this.message.chat.id
                val senderId = message.from!!.id
                val room = database.getRoomByTelegramId(chatId)
                if (room == null){
                    bot.sendMessageMultiLanguage(
                        ChatId.fromId(chatId), setOf(), "hotel_room_unknown", warn = false
                    )
                }
                else if (!room.finished && !room.canceledByAuthor && !room.canceledByPatron) {
                    val wish = room.wish
                    if (wish == null){
                        bot.sendMessageMultiLanguage(
                            ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_wish_unknown", warn = false
                        )
                    }
                    else if (senderId == wish.author.telegramId && !room.reportedByAuthor) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_report", warn = false)
                        val report = Report(database.generateNewReportId(), wish.author, wish.patron!!, "")
                        database.registerReport(report)
                        room.reportedByAuthor = true
                        database.updateRoom(room)
                    } else if (senderId == wish.patron!!.telegramId && !room.reportedByPatron) {
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_report", warn = false)
                        val report = Report(database.generateNewReportId(), wish.patron!!, wish.author, "")
                        database.registerReport(report)
                        room.reportedByPatron = true
                        database.updateRoom(room)
                    }
                    else{
                        bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_report_again", warn = false)
                    }
                }
                else{
                    bot.sendMessageMultiLanguage(ChatId.fromId(chatId), room.getLanguageCodes(), "hotel_room_closed", warn = false)
                }
            }
        }
    }
}