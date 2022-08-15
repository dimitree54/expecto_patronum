package we.rashchenko.patronum.ui.telegram.hotel.moderator

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.errors.UserReadableError
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.ui.telegram.bot.sendErrorMultiLanguage

class SafeRoomCommandHandler(
    private val database: Database,
    private val command: String,
    private val handleRoomCommand: (Bot, Long, ChatId.Id, WishRoom) -> Unit,
) : Handler {
    override fun checkUpdate(update: Update) = update.message?.text?.startsWith("/$command")?:false

    override fun handleUpdate(bot: Bot, update: Update) {
        val message = update.message ?: return
        val chatIdLong = message.chat.id
        val chatId = ChatId.fromId(chatIdLong)
        val senderId = message.from!!.id
        var languages = setOf<String>()
        try {
            val room = database.getRoomByTelegramId(chatIdLong)
            languages = room.getLanguageCodes()
            handleRoomCommand(bot, senderId, chatId, room)
        } catch (error: UserReadableError) {
            bot.sendErrorMultiLanguage(chatId, languages, error)
        }
    }
}