package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.getLocalisedMessage

class MenuHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val isUserFulfilling: (Long) -> Boolean,
    private val isUserHaveWishes: (Long) -> Boolean,
    private val onMakeWishPressed: (Long) -> Unit,
    private val onDoGoodPressed: (Long) -> Unit,
    private val onCancelFulfillmentPressed: (Long) -> Unit,
    private val onMyWishesPressed: (Long) -> Unit,
) : Handler {
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        // TODO show stats and greetings
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
        update.callbackQuery?.let {
            when (it.data) {
                "make_a_wish" -> onMakeWishPressed(user.id)
                "do_good" -> onDoGoodPressed(user.id)
                "cancel_fulfillment" -> onCancelFulfillmentPressed(user.id)
                "my_wishes" -> onMyWishesPressed(user.id)
            }
        }
    }
}