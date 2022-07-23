package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class BrowserHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onMatch: (Long, Wish) -> Unit,
    private val onSkip: (Long, Wish) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {
    private val ticketsQueue = mutableMapOf<Long, ArrayDeque<Wish>>()
    fun registerSearchResults(telegramUserId: Long, searchResults: Iterable<Wish>) {
        searchResults.toList().let{
            if (it.isNotEmpty()){
                ticketsQueue[telegramUserId] = ArrayDeque(it)
            }
        }
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {

        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message
        val tickets = ticketsQueue[user.id]

        val cancelMessage = getLocalisedMessage("cancel_message", user.languageCode)
        val acceptMessage = getLocalisedMessage("accept", user.languageCode)
        val skipMessage = getLocalisedMessage("skip", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        val acceptButton = KeyboardButton(acceptMessage)
        val skipButton = KeyboardButton(skipMessage)
        if (message?.text == cancelMessage || tickets == null || tickets.isEmpty()) {
            if (tickets == null || tickets.isEmpty()) {
                bot.sendMessage(chatId, getLocalisedMessage("no_results", user.languageCode))
            }
            ticketsQueue.remove(user.id)
            onCancel(user.id)
            return
        }
        else if (message?.text == acceptMessage){
            bot.sendMessage(chatId, getLocalisedMessage("connect", user.languageCode))
            ticketsQueue.remove(user.id)
            onMatch(user.id, tickets.first())
            return
        }
        else if (message?.text == skipMessage){
            onSkip(user.id, tickets.first())
            tickets.removeFirst()
        }

        sendWishCard(bot, user, chatId, tickets.first())
        bot.sendMessage(chatId = chatId, text = getLocalisedMessage("accept_prompt", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(
                keyboard = listOf(
                    listOf(acceptButton, skipButton),
                    listOf(cancelButton)
                )
            )
        )
    }
}