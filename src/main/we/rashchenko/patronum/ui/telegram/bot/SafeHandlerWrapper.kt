package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import we.rashchenko.patronum.errors.UserReadableError

class SafeHandlerWrapper(private val baseHandler: Handler, private val repeater: Repeater) : Handler {
    override fun checkUpdate(update: Update) = baseHandler.checkUpdate(update)

    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val languages = user.languageCode?.let { setOf(it) } ?: setOf()
        try {
            baseHandler.handleUpdate(bot, update)
        } catch (error: UserReadableError) {
            bot.sendErrorMultiLanguage(chatId, languages, error)
            repeater.requestRepeat()
        }
    }

}