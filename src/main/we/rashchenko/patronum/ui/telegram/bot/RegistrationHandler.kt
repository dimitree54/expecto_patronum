package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class RegistrationHandler(
    private val externalCheckUpdate: (Long) -> Boolean, private val onSuccessfulRegistration: (User) -> Unit
) : Handler {

    private enum class CallBackMessages(val value: String) {
        ACCEPT("accept"),
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        update.message?.let {
            val chatId = ChatId.fromId(it.chat.id)
            val languageCode = it.from?.languageCode
            if (it.text == "/start") {
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
                for (i in 1..6){
                    bot.sendMessage(
                        chatId = chatId,
                        text = getLocalisedMessage("registration_license_$i", languageCode),
                        disableWebPagePreview = true
                    )
                }
                bot.sendMessage(
                    chatId = chatId,
                    text = getLocalisedMessage("registration_accept_prompt", languageCode),
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = getLocalisedMessage("registration_accept_answer", languageCode), callbackData = CallBackMessages.ACCEPT.value
                            )
                        )
                    )
                )
            }
        } ?: update.callbackQuery?.let {
            if (it.data == CallBackMessages.ACCEPT.value) {
                val chatId = it.message?.chat?.id ?: return
                bot.sendMessage(
                    ChatId.fromId(chatId), text = getLocalisedMessage("registration_done", it.from.languageCode)
                )
                onSuccessfulRegistration(it.from)
            }
        }
    }
}