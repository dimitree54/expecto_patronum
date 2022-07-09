package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.Wish
import we.rashchenko.patronum.getLocalisedMessage


class MakeAWishHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onWishCreated: (Long, Wish) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {
    enum class MakeAWishState {
        ASK_FOR_TITLE, WAIT_FOR_TITLE, ASK_FOR_DESCRIPTION, WAIT_FOR_DESCRIPTION, ASK_FOR_LOCATION, WAIT_FOR_LOCATION, ASK_FOR_RADIUS, WAIT_FOR_RADIUS, ASK_FOR_CONFIRMATION, WAIT_FOR_CONFIRMATION
    }

    private val maxTitleLength: Int
    private val maxDescriptionLength: Int

    init {
        val properties = java.util.Properties()
        properties.load(ClassLoader.getSystemResourceAsStream("application.properties"))
        maxTitleLength = (properties["limit_len.title"] as String).toInt()
        maxDescriptionLength = (properties["limit_len.description"] as String).toInt()
    }

    private val userStates = mutableMapOf<Long, MakeAWishState>()
    private val drafts = mutableMapOf<Long, Wish>()
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it.id) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message

        val cancelMessage = getLocalisedMessage("cancel_message", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        if (message?.text == "cancel") {
            userStates.remove(user.id)
            drafts.remove(user.id)
            onCancel(user.id)
            return
        }

        val wishDraft = drafts.getOrPut(user.id) { Wish() }
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
                    } else break
                }
            }
        }
    }

    private fun askAndWaitForAnswer(
        answer: Message?, sendRequestMessage: () -> Unit, checkValidText: (Message?) -> Boolean
    ): Message? {
        return if (checkValidText(answer)) {
            answer
        } else {
            sendRequestMessage()
            null
        }
    }

    private fun checkValidTitle(message: Message?): Boolean {
        return message?.text?.let { it.length <= maxTitleLength } ?: false
    }

    private fun checkValidDescription(message: Message?): Boolean {
        return message?.text?.let { it.length <= maxDescriptionLength } ?: false
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
                maxDescriptionLength
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
            chatId = chatId, text = getLocalisedMessage("request_radius", user.languageCode).format(
                maxDescriptionLength
            ), replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
    }

    private fun requestConfirmation(
        bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton, wish: Wish, confirmationAnswer: String
    ) {
        bot.sendMessage(
            chatId = chatId, text = getLocalisedMessage("request_confirmation_card", user.languageCode)
        )
        sendWishCard(bot, user, chatId, wish)
        val confirmButton = KeyboardButton(getLocalisedMessage("confirm", user.languageCode))
        bot.sendMessage(
            chatId = chatId,
            text = confirmationAnswer,
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(confirmButton, cancelButton)), resizeKeyboard = true)
        )
    }

    private fun isTitleStageDone(
        bot: Bot, wishDraft: Wish, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val title = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestTitle(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidTitle
        )?.text
        return if (title != null) {
            userStates[user.id] = MakeAWishState.ASK_FOR_DESCRIPTION
            wishDraft.title = title
            true
        } else {
            false
        }
    }

    private fun isDescriptionStageDone(
        bot: Bot, wishDraft: Wish, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val description = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestDescription(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidDescription
        )?.text
        return if (description != null) {
            userStates[user.id] = MakeAWishState.ASK_FOR_LOCATION
            wishDraft.description = description
            true
        } else {
            false
        }
    }

    private fun isLocationStageDone(
        bot: Bot, wishDraft: Wish, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val location = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestLocation(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidLocation
        )?.location
        return if (location != null) {
            userStates[user.id] = MakeAWishState.ASK_FOR_LOCATION
            wishDraft.location = location
            true
        } else {
            false
        }
    }

    private fun isRadiusStageDone(
        bot: Bot, wishDraft: Wish, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val radius = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestRadius(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidRadius
        )?.text
        return if (radius != null) {
            userStates[user.id] = MakeAWishState.ASK_FOR_CONFIRMATION
            wishDraft.radius = radius.toDouble()
            true
        } else {
            false
        }
    }

    private fun isConfirmationStageDone(
        bot: Bot, wishDraft: Wish, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val confirmationAnswer = getLocalisedMessage("request_confirmation_correct", user.languageCode)
        val confirmation = askAndWaitForAnswer(message, sendRequestMessage = {
            requestConfirmation(
                bot, user, chatId, cancelButton, wishDraft, confirmationAnswer
            )
        }, checkValidText = { checkConfirmation(it, confirmationAnswer) })
        return if (confirmation != null) {
            bot.sendMessage(chatId, getLocalisedMessage("request_confirmation_done", user.languageCode))
            true
        } else {
            false
        }
    }
}