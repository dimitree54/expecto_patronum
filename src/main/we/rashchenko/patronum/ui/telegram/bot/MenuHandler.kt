package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import we.rashchenko.patronum.database.stats.GlobalStats
import we.rashchenko.patronum.database.stats.UserStats
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.Wish

class MenuHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val getUserStatistics: (Long) -> UserStats,
    private val getGlobalStatistics: () -> GlobalStats,
    private val getWishUserFulfilling: (Long) -> Wish?,
    private val onMakeWishPressed: (Long) -> Unit,
    private val onSearchPressed: (Long) -> Unit,
    private val onCancelFulfillmentPressed: (Long) -> Unit,
    private val onMyWishesPressed: (Long) -> Unit,
) : Handler {

    private enum class CallBackMessages(val value: String) {
        MAKE_WISH("menu_make_wish"), DO_WISH("menu_do_good"), CANCEL_FULFILLMENT("cancel_fulfillment"), MY_WISHES("menu_my_wishes")
    }

    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val userStatistics = getUserStatistics(user.id)
        val wishUserFulfilling = getWishUserFulfilling(user.id)

        val buttons = buildAnswerButtons(user, wishUserFulfilling != null, userStatistics.myWishesActive > 0)

        update.callbackQuery?.let {
            when (it.data) {
                CallBackMessages.MAKE_WISH.value -> onMakeWishPressed(user.id)
                CallBackMessages.DO_WISH.value -> onSearchPressed(user.id)
                CallBackMessages.CANCEL_FULFILLMENT.value -> onCancelFulfillmentPressed(user.id)
                CallBackMessages.MY_WISHES.value -> onMyWishesPressed(user.id)
                else -> null
            }
        } ?: run{
            sendGreetings(bot, user, chatId, userStatistics, getGlobalStatistics())
            if (wishUserFulfilling != null) {
                bot.sendMessage(chatId, getLocalisedMessage("menu_wish_taken", user.languageCode))
                sendWishCard(bot, chatId, wishUserFulfilling, setOf(user.languageCode))
            }
            bot.sendMessage(
                chatId = chatId,
                text = getLocalisedMessage("menu_title", user.languageCode),
                replyMarkup = InlineKeyboardMarkup.create(buttons)
            )
        }
    }

    private fun sendGreetings(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStats, globalStats: GlobalStats) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("registration_info", user.languageCode),
            parseMode = ParseMode.MARKDOWN,
            disableWebPagePreview = true
        )
        sendUserStatistics(bot, user, chatId, stats, globalStats)
    }

    private fun buildAnswerButtons(
        user: User, isUserFulfilling: Boolean, isUserHaveWishes: Boolean
    ): MutableList<InlineKeyboardButton.CallbackData> {
        val buttons = mutableListOf(
            InlineKeyboardButton.CallbackData(
                text = getLocalisedMessage("menu_make_wish", user.languageCode), callbackData = CallBackMessages.MAKE_WISH.value
            )
        )
        if (isUserFulfilling) {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("menu_cancel_fulfillment", user.languageCode),
                    callbackData = CallBackMessages.CANCEL_FULFILLMENT.value
                ),
            )
        } else {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("menu_do_good", user.languageCode), callbackData = CallBackMessages.DO_WISH.value
                )
            )
        }
        if (isUserHaveWishes) {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("menu_my_wishes", user.languageCode), callbackData = CallBackMessages.MY_WISHES.value
                )
            )
        }
        return buttons
    }
}