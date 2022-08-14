package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class BrowserHandler(
    private val externalCheckUpdate: (User) -> Boolean,
    private val onMatch: (User, Wish) -> Unit,
    private val onSkip: (User, Wish) -> Unit,
    private val onCancel: (User) -> Unit,
) : Handler {
    private enum class State {
        SEND_FIRST_CARD, WAIT_FOR_REACTION
    }
    private val states = mutableMapOf<Long, State>()
    private val ticketsQueue = mutableMapOf<Long, ArrayDeque<Wish>>()
    fun registerSearchResults(telegramUserId: Long, searchResults: Iterable<Wish>) {
        searchResults.toList().let{
            if (it.isNotEmpty()){
                ticketsQueue[telegramUserId] = ArrayDeque(it)
            }
        }
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {

        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message
        val tickets = ticketsQueue[user.id]
        val state = states.getOrPut(user.id) { State.SEND_FIRST_CARD }

        val cancelMessage = getLocalisedMessage("browser_cancel", user.languageCode)
        val acceptMessage = getLocalisedMessage("browser_wish_accept_answer", user.languageCode)
        val skipMessage = getLocalisedMessage("browser_wish_skip", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        val acceptButton = KeyboardButton(acceptMessage)
        val skipButton = KeyboardButton(skipMessage)

        if (tickets.isNullOrEmpty()){
            bot.clearKeyboard(chatId, getLocalisedMessage("browser_no_results", user.languageCode))
            states.remove(user.id)
            ticketsQueue.remove(user.id)
            onCancel(user)
            return
        }
        else if (state == State.WAIT_FOR_REACTION && message?.text == cancelMessage) {
            bot.clearKeyboard(chatId, getLocalisedMessage("browser_cancel", user.languageCode))
            states.remove(user.id)
            ticketsQueue.remove(user.id)
            onCancel(user)
            return
        }
        else if ((state == State.WAIT_FOR_REACTION && message?.text == acceptMessage)){
            bot.clearKeyboard(chatId, getLocalisedMessage("browser_connect", user.languageCode))
            states.remove(user.id)
            ticketsQueue.remove(user.id)
            onMatch(user, tickets.first())
            return
        }
        else if ((state == State.WAIT_FOR_REACTION && message?.text == skipMessage)){
            onSkip(user, tickets.first())
            tickets.removeFirst()
        }
        else{
            sendWishCard(bot, chatId, tickets.first(), setOf(user.languageCode))
            bot.sendMessage(chatId = chatId, text = getLocalisedMessage("browser_wish_accept_prompt", user.languageCode),
                replyMarkup = KeyboardReplyMarkup(
                    keyboard = listOf(
                        listOf(acceptButton, skipButton),
                        listOf(cancelButton)
                    )
                )
            )
            states[user.id] = State.WAIT_FOR_REACTION
        }
    }
}