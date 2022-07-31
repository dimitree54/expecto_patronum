package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class ManageWishHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onWishDelete: (Long, Wish) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {

    private enum class State {
        SEND_CARD, WAIT_FOR_REACTION
    }
    private val states = mutableMapOf<Long, State>()

    private val userWishes = mutableMapOf<Long, Wish>()
    fun registerChosenWish(userId: Long, wish: Wish) {
        userWishes[userId] = wish
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message
        val state = states.getOrPut(user.id) { State.SEND_CARD }

        val deleteMessage = getLocalisedMessage("manage_wish_withdraw", user.languageCode)
        val deleteButton = KeyboardButton(deleteMessage)

        val cancelMessage = getLocalisedMessage("manage_wish_cancel", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)

        if (state == State.WAIT_FOR_REACTION && message?.text == cancelMessage) {
            bot.clearKeyboard(chatId, cancelMessage)
            states.remove(user.id)
            userWishes.remove(user.id)
            onCancel(user.id)
            return
        }

        askAndWaitForAnswer(message, sendRequestMessage = {
            states[user.id] = State.WAIT_FOR_REACTION
            sendWishCard(bot, chatId, userWishes[user.id]!!, setOf(user.languageCode))
            bot.sendMessage(
                chatId,
                getLocalisedMessage("manage_wish_delete", user.languageCode),
                replyMarkup = KeyboardReplyMarkup(
                    keyboard = listOf(
                        listOf(deleteButton),
                        listOf(cancelButton)
                    )
                )
            )
        }, checkValidText = {state == State.WAIT_FOR_REACTION && it?.text == deleteMessage})?: return
        bot.clearKeyboard(chatId, getLocalisedMessage("manage_wish_remove", user.languageCode))
        onWishDelete(user.id, userWishes[user.id]!!)
        userWishes.remove(user.id)
        states.remove(user.id)
    }
}