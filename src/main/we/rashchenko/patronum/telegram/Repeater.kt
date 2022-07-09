package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update

class Repeater : Handler {
    private var repeatRequested = false
    fun requestRepeat() {
        repeatRequested = true
    }

    private val handlers = mutableListOf<Handler>()
    fun addHandler(handler: Handler) {
        handlers.add(handler)
    }

    override fun checkUpdate(update: Update): Boolean {
        return true
    }

    override fun handleUpdate(bot: Bot, update: Update) {
        if (repeatRequested){
            repeatRequested = false
            handlers.filter { it.checkUpdate(update) }.forEach { it.handleUpdate(bot, update) }
        }
    }
}