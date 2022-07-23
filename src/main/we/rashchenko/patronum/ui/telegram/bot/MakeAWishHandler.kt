package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.search.SearchInfoDraft
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.WishDraft
import we.rashchenko.patronum.wishes.strings.BadLengthError
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title


class MakeAWishHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onWishCreated: (Long, WishDraft) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {
    private enum class MakeAWishState {
        ASK_FOR_TITLE, WAIT_FOR_TITLE, ASK_FOR_DESCRIPTION, WAIT_FOR_DESCRIPTION, ASK_FOR_LOCATION, WAIT_FOR_LOCATION, ASK_FOR_RADIUS, WAIT_FOR_RADIUS, ASK_FOR_CONFIRMATION, WAIT_FOR_CONFIRMATION
    }

    private val maxTitleLength: Int
    private val maxDescriptionLength: Int

    init {
        val properties = java.util.Properties()
        properties.load(ClassLoader.getSystemResourceAsStream("reputation.properties"))
        maxTitleLength = (properties["limit_len.title"] as String).toInt()
        maxDescriptionLength = (properties["limit_len.description"] as String).toInt()
    }

    private val userStates = mutableMapOf<Long, MakeAWishState>()
    private val drafts = mutableMapOf<Long, WishDraft>()
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message

        val cancelMessage = getLocalisedMessage("cancel_message", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        if (message?.text == cancelMessage) {
            userStates.remove(user.id)
            drafts.remove(user.id)
            onCancel(user.id)
            return
        }

        val wishDraft = drafts.getOrPut(user.id) { WishDraft() }
        while (true) {
            when (userStates.getOrPut(user.id) { MakeAWishState.ASK_FOR_TITLE }) {
                MakeAWishState.ASK_FOR_TITLE, MakeAWishState.WAIT_FOR_TITLE -> {
                    if (isTitleStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                MakeAWishState.ASK_FOR_DESCRIPTION, MakeAWishState.WAIT_FOR_DESCRIPTION -> {
                    if (isDescriptionStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                MakeAWishState.ASK_FOR_LOCATION, MakeAWishState.WAIT_FOR_LOCATION -> {
                    if (isLocationStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                MakeAWishState.ASK_FOR_RADIUS, MakeAWishState.WAIT_FOR_RADIUS -> {
                    if (isRadiusStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                MakeAWishState.ASK_FOR_CONFIRMATION, MakeAWishState.WAIT_FOR_CONFIRMATION -> {
                    if (isConfirmationStageDone(bot, wishDraft, message, user, chatId, cancelButton)) {
                        onWishCreated(user.id, wishDraft)
                        return
                    } else break
                }
            }
        }
    }

    private fun checkValidTitle(message: Message?): Boolean {
        val text = message?.text ?: return false
        return try {
            Title(text)
            true
        } catch (e: BadLengthError) {
            false
        }
    }

    private fun checkValidDescription(message: Message?): Boolean {
        val text = message?.text ?: return false
        return try {
            Description(text)
            true
        } catch (e: BadLengthError) {
            false
        }
    }

    private fun checkValidLocation(message: Message?): Boolean {
        return message?.location?.let { true } ?: false
    }

    private fun checkValidRadius(message: Message?): Boolean {
        return message?.text?.toDoubleOrNull()?.let { true } ?: false
    }

    private fun checkConfirmation(message: Message?, targetMessageText: String): Boolean {
        return message?.text?.let { it == targetMessageText } ?: false
    }

    private fun requestTitle(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_title", user.languageCode).format(
                maxTitleLength
            ), replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
    }

    private fun requestDescription(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_description", user.languageCode).format(
                maxDescriptionLength
            ), replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
    }

    private fun requestLocation(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        val shareLocationButton =
            KeyboardButton(getLocalisedMessage("location_share", user.languageCode), requestLocation = true)
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("request_location", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(shareLocationButton, cancelButton)), resizeKeyboard = true)
        )
    }

    private fun requestRadius(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("request_radius", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
    }

    private fun requestConfirmation(
        bot: Bot,
        user: User,
        chatId: ChatId.Id,
        cancelButton: KeyboardButton,
        wishDraft: WishDraft,
        confirmationAnswer: String
    ) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_confirmation_card", user.languageCode)
        )
        sendWishDraftCard(bot, user, chatId, wishDraft)
        val confirmButton = KeyboardButton(getLocalisedMessage("request_confirmation_submit", user.languageCode))
        bot.sendMessage(
            chatId = chatId,
            text = confirmationAnswer,
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(confirmButton, cancelButton)), resizeKeyboard = true)
        )
    }

    private fun isTitleStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val title = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestTitle(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidTitle
        )?.text ?: return false
        userStates[user.id] = MakeAWishState.ASK_FOR_DESCRIPTION
        wishDraft.title = Title(title)
        return true
    }

    private fun isDescriptionStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val description = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestDescription(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidDescription
        )?.text ?: return false
        userStates[user.id] = MakeAWishState.ASK_FOR_LOCATION
        wishDraft.description = Description(description)
        return true
    }

    private fun isLocationStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val location = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestLocation(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidLocation
        )?.location ?: return false
        userStates[user.id] = MakeAWishState.ASK_FOR_RADIUS
        wishDraft.searchInfoDraft = SearchInfoDraft().also {
            it.location = we.rashchenko.patronum.search.geo.Location(location.longitude, location.latitude)
        }
        return true
    }

    private fun isRadiusStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val radius = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestRadius(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidRadius
        )?.text ?: return false
        userStates[user.id] = MakeAWishState.ASK_FOR_CONFIRMATION
        wishDraft.searchInfoDraft!!.radius = radius.toFloat()
        return true
    }

    private fun isConfirmationStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val confirmationAnswer = getLocalisedMessage("request_confirmation_correct", user.languageCode)
        askAndWaitForAnswer(message, sendRequestMessage = {
            requestConfirmation(
                bot, user, chatId, cancelButton, wishDraft, confirmationAnswer
            )
        }, checkValidText = { checkConfirmation(it, confirmationAnswer) }) ?: return false
        bot.sendMessage(chatId, getLocalisedMessage("request_confirmation_wish_done", user.languageCode))
        return true
    }
}