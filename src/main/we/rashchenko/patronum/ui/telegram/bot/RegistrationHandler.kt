package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class RegistrationHandler(
    private val externalCheckUpdate: (Long) -> Boolean, private val onSuccessfulRegistration: (User) -> Unit
) : Handler {

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        update.message?.let {
            val chatId = ChatId.fromId(it.chat.id)
            val languageCode = it.from?.languageCode
            if (it.text == "/start") {
                bot.sendMessage(
                    chatId = chatId, text = getLocalisedMessage("info", languageCode),
                    parseMode = ParseMode.MARKDOWN,
                    disableWebPagePreview = true
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = getLocalisedMessage("onboarding", languageCode),
                    parseMode = ParseMode.MARKDOWN,
                    disableWebPagePreview = true
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = getLocalisedMessage("accept_prompt", languageCode),
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = getLocalisedMessage("accept", languageCode), callbackData = "accept"
                            )
                        )
                    )
                )
            }
        } ?: update.callbackQuery?.let {
            if (it.data == "accept") {
                val chatId = it.message?.chat?.id ?: return
                bot.sendMessage(
                    ChatId.fromId(chatId), text = getLocalisedMessage("after_onboarding", it.from.languageCode)
                )
                onSuccessfulRegistration(it.from)
            }
        }
    }
}