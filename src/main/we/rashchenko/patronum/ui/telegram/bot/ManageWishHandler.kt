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
    private val userWishes = mutableMapOf<Long, Wish>()
    fun registerChosenWish(userId: Long, wish: Wish) {
        userWishes[userId] = wish
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message

        val deleteMessage = getLocalisedMessage("delete_wish", user.languageCode)
        val deleteButton = KeyboardButton(deleteMessage)

        val cancelMessage = getLocalisedMessage("cancel_message", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)

        if (message?.text == cancelMessage) {
            onCancel(user.id)
            return
        }

        val deleteRequest = askAndWaitForAnswer(message, sendRequestMessage = {
            bot.sendMessage(
                chatId,
                getLocalisedMessage("request_wish_delete", user.languageCode),
                replyMarkup = KeyboardReplyMarkup(
                    keyboard = listOf(
                        listOf(deleteButton),
                        listOf(cancelButton)
                    )
                )
            )
        }, checkValidText = {it?.text == deleteMessage})?.text?.toIntOrNull()
        if (deleteRequest != null) {
            onWishDelete(user.id, userWishes[user.id]!!)
            userWishes.remove(user.id)
        }
    }
}