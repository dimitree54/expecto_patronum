package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.Wish


class MyWishesHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val getUserWishes: (Long) -> List<Wish>,
    private val onWishChosen: (Long, Wish) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {

    private enum class State {
        SEND_CARDS, WAIT_FOR_REACTION
    }

    private val states = mutableMapOf<Long, State>()

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message
        val wishes = getUserWishes(user.id)
        val state = states.getOrPut(user.id) { State.SEND_CARDS }

        val summary = wishes.mapIndexed { index, wish ->
            "$index: ${wish.title.text}"
        }.joinToString("\n")

        val cancelMessage = getLocalisedMessage("cancel", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)

        if (state == State.WAIT_FOR_REACTION && message?.text == cancelMessage) {
            bot.clearKeyboard(chatId, cancelMessage)
            onCancel(user.id)
            states.remove(user.id)
            return
        }

        val chosenIndex = askAndWaitForAnswer(message, sendRequestMessage = {
            bot.sendMessage(chatId, summary)
            bot.sendMessage(
                chatId, getLocalisedMessage("request_wish_number", user.languageCode),
                replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)))
            )
        }, checkValidText = {
            state == State.WAIT_FOR_REACTION && it?.text?.toIntOrNull()?.let { index ->
                index in wishes.indices
            } ?: false
        })?.text?.toIntOrNull()
        if (chosenIndex != null) {
            onWishChosen(user.id, wishes[chosenIndex])
            states.remove(user.id)
        }
    }
}