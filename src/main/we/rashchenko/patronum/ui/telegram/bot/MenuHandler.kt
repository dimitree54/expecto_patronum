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
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false

    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val userStatistics = getUserStatistics(user.id)
        val wishUserFulfilling = getWishUserFulfilling(user.id)

        sendGreetings(bot, user, chatId, userStatistics, getGlobalStatistics())
        if (wishUserFulfilling != null) {
            bot.sendMessage(chatId, getLocalisedMessage("you_fulfilling_a_wish", user.languageCode))
            sendWishCard(bot, chatId, wishUserFulfilling, setOf(user.languageCode))
        }

        val buttons = buildAnswerButtons(user, wishUserFulfilling != null, userStatistics.myWishesActive > 0)

        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("menu", user.languageCode),
            replyMarkup = InlineKeyboardMarkup.create(buttons)
        )
        update.callbackQuery?.let {
            when (it.data) {
                "make_a_wish" -> onMakeWishPressed(user.id)
                "do_good" -> onSearchPressed(user.id)
                "cancel_fulfillment" -> onCancelFulfillmentPressed(user.id)
                "my_wishes" -> onMyWishesPressed(user.id)
            }
        }
    }

    private fun sendGreetings(bot: Bot, user: User, chatId: ChatId.Id, stats: UserStats, globalStats: GlobalStats) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("info", user.languageCode),
            parseMode = ParseMode.MARKDOWN,
            disableWebPagePreview = true
        )
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("greetings", user.languageCode)
        )
        sendUserStatistics(bot, user, chatId, stats, globalStats)
    }

    private fun buildAnswerButtons(
        user: User, isUserFulfilling: Boolean, isUserHaveWishes: Boolean
    ): MutableList<InlineKeyboardButton.CallbackData> {
        val buttons = mutableListOf(
            InlineKeyboardButton.CallbackData(
                text = getLocalisedMessage("make_a_wish", user.languageCode), callbackData = "make_a_wish"
            )
        )
        if (isUserFulfilling) {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("cancel_fulfillment", user.languageCode),
                    callbackData = "cancel_fulfillment"
                ),
            )
        } else {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("do_good", user.languageCode), callbackData = "do_good"
                )
            )
        }
        if (isUserHaveWishes) {
            buttons.add(
                InlineKeyboardButton.CallbackData(
                    text = getLocalisedMessage("my_wishes", user.languageCode), callbackData = "my_wishes"
                )
            )
        }
        return buttons
    }
}