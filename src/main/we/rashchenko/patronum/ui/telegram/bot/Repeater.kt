package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update

class Repeater : Handler {
    private var repeatRequested = false
    private var depth = 0
    private val maxDepth = 10
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
        if (depth > maxDepth) {
            throw IllegalStateException("Repeating in a loop")
        }
        if (repeatRequested){
            repeatRequested = false
            depth++
            handlers.filter { it.checkUpdate(update) }.forEach { it.handleUpdate(bot, update) }
            depth--
        }
    }
}