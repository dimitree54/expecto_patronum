package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class RegistrationHandler(
    private val externalCheckUpdate: (User) -> Boolean,
    private val checkRegisteredInHotel: (User) -> Boolean,
    private val onSuccessfulRegistration: (User) -> Unit,
) : Handler {

    private enum class State {
        SEND_LICENSE, ASK_FOR_HOTEL_CHECK_IN, WAIT_FOR_HOTEL_CHECK_IN
    }

    private val states = mutableMapOf<Long, State>()

    private enum class CallBackMessages(val value: String) {
        ACCEPT("accept"),
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val languageCode = user.languageCode
        val message = update.message
        states.getOrPut(user.id) { State.SEND_LICENSE }

        if (states[user.id] == State.SEND_LICENSE && message?.text == "/start") {
            val result = bot.sendMessage(
                chatId = chatId, text = getLocalisedMessage("registration_info", languageCode),
                parseMode = ParseMode.MARKDOWN,
                disableWebPagePreview = true
            )
            bot.unpinAllChatMessages(chatId)
            bot.pinChatMessage(chatId, result.get().messageId)
            bot.sendMessage(
                chatId = chatId,
                text = getLocalisedMessage("registration_license_intro", languageCode),
                disableWebPagePreview = true
            )
            for (i in 1..6) {
                bot.sendMessage(
                    chatId = chatId,
                    text = getLocalisedMessage("registration_license_$i", languageCode),
                    disableWebPagePreview = true
                )
            }
            states[user.id] = State.ASK_FOR_HOTEL_CHECK_IN
        }
        if (states[user.id] == State.WAIT_FOR_HOTEL_CHECK_IN) {
            if (checkRegisteredInHotel(user) && update.callbackQuery?.data == CallBackMessages.ACCEPT.value) {
                bot.sendMessage(
                    chatId, text = getLocalisedMessage("registration_done", languageCode)
                )
                onSuccessfulRegistration(user)
            } else {
                states[user.id] = State.ASK_FOR_HOTEL_CHECK_IN
            }
        }
        if (states[user.id] == State.ASK_FOR_HOTEL_CHECK_IN){
            bot.sendMessage(
                chatId = chatId,
                text = getLocalisedMessage("registration_in_hotel_prompt", languageCode),
                replyMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = getLocalisedMessage("registration_in_hotel_done", languageCode),
                            callbackData = CallBackMessages.ACCEPT.value
                        )
                    )
                )
            )
            states[user.id] = State.WAIT_FOR_HOTEL_CHECK_IN
        }
    }
}