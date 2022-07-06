package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.getLocalisedMessage

class MenuHandler(
    private val externalCheckUpdate: (Update) -> Boolean,
    private val isUserFulfilling: (Long) -> Boolean,
    private val isUserHaveWishes: (Long) -> Boolean,
    private val onMakeWishPressed: (Long) -> Unit,
    private val onDoGoodPressed: (Long) -> Unit,
    private val onCancelFulfillmentPressed: (Long) -> Unit,
    private val onMyWishesPressed: (Long) -> Unit,
) : Handler {
    override fun checkUpdate(update: Update) = externalCheckUpdate(update)

    override fun handleUpdate(bot: Bot, update: Update) {
        update.message?.let {
            // TODO show stats and greetings
            val chatId = ChatId.fromId(it.chat.id)
            val user = it.from ?: return
            val buttons = mutableListOf(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("make_a_wish", user.languageCode), callbackData = "make_a_wish"
                ), InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("do_good", user.languageCode), callbackData = "do_good"
                )
            )
            if (isUserFulfilling(user.id)) {
                buttons.add(
                    InlineKeyboardButton.CallbackData(
                        text = getLocalisedMessage("cancel_fulfillment", user.languageCode),
                        callbackData = "cancel_fulfillment"
                    ),
                )
            }
            if (isUserHaveWishes(user.id)) {
                buttons.add(
                    InlineKeyboardButton.CallbackData(
                        text = getLocalisedMessage("my_wishes", user.languageCode), callbackData = "my_wishes"
                    )
                )
            }

            bot.sendMessage(
                chatId = chatId,
                text = getLocalisedMessage("menu", user.languageCode),
                replyMarkup = InlineKeyboardMarkup.create(buttons)
            )
        } ?: update.callbackQuery?.let {
            val chatId = it.message?.chat?.id ?: return
            when (it.data) {
                "make_a_wish" -> onMakeWishPressed(chatId)
                "do_good" -> onDoGoodPressed(chatId)
                "cancel_fulfillment" -> onCancelFulfillmentPressed(chatId)
                "my_wishes" -> onMyWishesPressed(chatId)
            }
        }
    }
}