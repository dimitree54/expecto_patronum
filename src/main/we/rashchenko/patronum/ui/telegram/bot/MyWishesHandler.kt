package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.Wish


class MyWishesHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val getUserWishes: (Long) -> List<Wish>,
    private val onWishChosen: (Long, Wish) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message
        val wishes = getUserWishes(user.id)

        val summary = wishes.mapIndexed { index, wish ->
            "$index: ${wish.title}"
        }.joinToString("\n")

        val cancelMessage = getLocalisedMessage("cancel_message", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)

        if (message?.text == cancelMessage) {
            onCancel(user.id)
            return
        }

        val chosenIndex = askAndWaitForAnswer(message, sendRequestMessage = {
            bot.sendMessage(chatId, summary)
            bot.sendMessage(chatId, getLocalisedMessage("request_wish_number", user.languageCode),
                replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton))))
        }, checkValidText = {
            it?.text?.toIntOrNull()?.let { index ->
                index in wishes.indices
            } ?: false
        })?.text?.toIntOrNull()
        if (chosenIndex != null) {
            onWishChosen(user.id, wishes[chosenIndex])
        }
    }
}