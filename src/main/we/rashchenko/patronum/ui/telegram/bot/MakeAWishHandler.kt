package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.errors.UserReadableError
import we.rashchenko.patronum.search.SearchInfoDraft
import we.rashchenko.patronum.ui.messages.getLocalisedMessage
import we.rashchenko.patronum.wishes.WishDraft
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title


class MakeAWishHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onWishCreated: (Long, WishDraft) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {
    private enum class State {
        ASK_FOR_TITLE, WAIT_FOR_TITLE, ASK_FOR_DESCRIPTION, WAIT_FOR_DESCRIPTION, ASK_FOR_LOCATION, WAIT_FOR_LOCATION, ASK_FOR_RADIUS, WAIT_FOR_RADIUS, ASK_FOR_CONFIRMATION, WAIT_FOR_CONFIRMATION
    }

    private val maxTitleLength: Int
    private val maxDescriptionLength: Int

    init {
        val properties = java.util.Properties()
        properties.load(ClassLoader.getSystemResourceAsStream("limits.properties"))
        maxTitleLength = (properties["maxTitleLength"] as String).toInt()
        maxDescriptionLength = (properties["maxDescriptionLength"] as String).toInt()
    }

    private val userStates = mutableMapOf<Long, State>()
    private val drafts = mutableMapOf<Long, WishDraft>()
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message

        val cancelMessage = getLocalisedMessage("cancel", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        if (message?.text == cancelMessage) {
            bot.clearKeyboard(chatId, cancelMessage)
            userStates.remove(user.id)
            drafts.remove(user.id)
            onCancel(user.id)
            return
        }

        val wishDraft = drafts.getOrPut(user.id) { WishDraft() }
        while (true) {
            when (userStates.getOrPut(user.id) { State.ASK_FOR_TITLE }) {
                State.ASK_FOR_TITLE, State.WAIT_FOR_TITLE -> {
                    if (isTitleStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                State.ASK_FOR_DESCRIPTION, State.WAIT_FOR_DESCRIPTION -> {
                    if (isDescriptionStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                State.ASK_FOR_LOCATION, State.WAIT_FOR_LOCATION -> {
                    if (isLocationStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                State.ASK_FOR_RADIUS, State.WAIT_FOR_RADIUS -> {
                    if (isRadiusStageDone(bot, wishDraft, message, user, chatId, cancelButton)) continue else break
                }
                State.ASK_FOR_CONFIRMATION, State.WAIT_FOR_CONFIRMATION -> {
                    if (isConfirmationStageDone(bot, wishDraft, message, user, chatId, cancelButton)) {
                        userStates.remove(user.id)
                        drafts.remove(user.id)
                        onWishCreated(user.id, wishDraft)
                        return
                    } else break
                }
            }
        }
    }

    private fun checkValidLocation(message: Message?): Boolean {
        return message?.location?.let { true } ?: false
    }

    private fun checkSkipLocation(message: Message?, skipMessage: String): Boolean {
        return message?.text == skipMessage
    }

    private fun checkValidRadius(message: Message?): Boolean {
        return message?.text?.toDoubleOrNull()?.let { true } ?: false
    }

    private fun checkConfirmation(message: Message?, targetMessageText: String): Boolean {
        return message?.text?.let { it == targetMessageText } ?: false
    }

    private fun requestTitle(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_title", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_TITLE
    }

    private fun requestDescription(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_description", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_DESCRIPTION
    }

    private fun requestLocation(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton, skipButton: KeyboardButton) {
        val shareLocationButton =
            KeyboardButton(getLocalisedMessage("location_share", user.languageCode), requestLocation = true)
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("request_location", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(shareLocationButton, skipButton, cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_LOCATION
    }

    private fun requestRadius(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("request_radius", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_RADIUS
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
        val confirmButton = KeyboardButton(confirmationAnswer)
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("request_confirmation_correct", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(confirmButton, cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_CONFIRMATION
    }

    private fun isTitleStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val title = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestTitle(bot, user, chatId, cancelButton) },
            checkValidText = { userStates[user.id] == State.WAIT_FOR_TITLE },
        )?.text ?: return false
        try{
            wishDraft.title = Title(title)
        }
        catch (e: UserReadableError){
            bot.sendMessage(chatId, e.getUserReadableMessage(user.languageCode))
            return false
        }
        userStates[user.id] = State.ASK_FOR_DESCRIPTION
        return true
    }

    private fun isDescriptionStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val description = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestDescription(bot, user, chatId, cancelButton) },
            checkValidText = { userStates[user.id] == State.WAIT_FOR_DESCRIPTION },
        )?.text ?: return false
        try{
            wishDraft.description = Description(description)
        }
        catch (e: UserReadableError){
            bot.sendMessage(chatId, e.getUserReadableMessage(user.languageCode))
            return false
        }
        userStates[user.id] = State.ASK_FOR_LOCATION
        return true
    }

    private fun isLocationStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val skipMessage = getLocalisedMessage("skip", user.languageCode)
        val skipButton = KeyboardButton(skipMessage)
        val answer = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestLocation(bot, user, chatId, cancelButton, skipButton) },
            checkValidText = { userStates[user.id] == State.WAIT_FOR_LOCATION && (checkValidLocation(it) || checkSkipLocation(it, skipMessage)) }
        ) ?: return false
        if (checkSkipLocation(answer, skipMessage)) {
            userStates[user.id] = State.ASK_FOR_CONFIRMATION
            wishDraft.searchInfoDraft = SearchInfoDraft()
        }
        else{
            val location = answer.location!!
            userStates[user.id] = State.ASK_FOR_RADIUS
            wishDraft.searchInfoDraft = SearchInfoDraft().also {
                it.location = we.rashchenko.patronum.search.geo.Location(location.longitude, location.latitude)
            }
        }
        return true
    }

    private fun isRadiusStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val radius = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestRadius(bot, user, chatId, cancelButton) },
            checkValidText = { userStates[user.id] == State.WAIT_FOR_RADIUS && checkValidRadius(it) }
        )?.text ?: return false
        userStates[user.id] = State.ASK_FOR_CONFIRMATION
        wishDraft.searchInfoDraft!!.radius = radius.toFloat()
        return true
    }

    private fun isConfirmationStageDone(
        bot: Bot, wishDraft: WishDraft, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val confirmationAnswer = getLocalisedMessage("request_confirmation_submit", user.languageCode)
        askAndWaitForAnswer(message, sendRequestMessage = {
            requestConfirmation(
                bot, user, chatId, cancelButton, wishDraft, confirmationAnswer
            )
        }, checkValidText = { userStates[user.id] == State.WAIT_FOR_CONFIRMATION && checkConfirmation(it, confirmationAnswer) }) ?: return false
        bot.sendMessage(chatId, getLocalisedMessage("request_confirmation_wish_done", user.languageCode), replyMarkup = ReplyKeyboardRemove())
        return true
    }
}