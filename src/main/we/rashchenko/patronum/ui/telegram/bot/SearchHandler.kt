package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.search.SearchInfoDraft
import we.rashchenko.patronum.ui.messages.getLocalisedMessage


class SearchHandler(
    private val externalCheckUpdate: (User) -> Boolean,
    private val onSearchRequestCreated: (User, SearchInfoDraft) -> Unit,
    private val onCancel: (User) -> Unit,
) : Handler {

    private enum class State {
        ASK_FOR_LOCATION, WAIT_FOR_LOCATION, SKIP_LOCATION, ASK_FOR_RADIUS, WAIT_FOR_RADIUS
    }

    private val userStates = mutableMapOf<Long, State>()
    private val drafts = mutableMapOf<Long, SearchInfoDraft>()
    override fun checkUpdate(update: Update) = getTelegramUser(update)?.let { externalCheckUpdate(it) } ?: false
    override fun handleUpdate(bot: Bot, update: Update) {
        val user = getTelegramUser(update) ?: return
        val chatId = getChatId(update) ?: return
        val message = update.message

        val cancelMessage = getLocalisedMessage("search_cancel", user.languageCode)
        val cancelButton = KeyboardButton(cancelMessage)
        if (message?.text == cancelMessage) {
            cleanup(bot, chatId, user, cancelMessage)
            onCancel(user)
            return
        }

        val requestDraft = drafts.getOrPut(user.id) { SearchInfoDraft() }
        while (true) {
            when (userStates.getOrPut(user.id) { State.ASK_FOR_LOCATION }) {
                State.ASK_FOR_LOCATION, State.WAIT_FOR_LOCATION -> {
                    if (isLocationStageDone(bot, requestDraft, message, user, chatId, cancelButton)) continue else break
                }

                State.ASK_FOR_RADIUS, State.WAIT_FOR_RADIUS -> {
                    if (isRadiusStageDone(bot, requestDraft, message, user, chatId, cancelButton)) {
                        cleanup(bot, chatId, user, getLocalisedMessage("search_in_progress", user.languageCode))
                        onSearchRequestCreated(user, requestDraft)
                        return
                    } else break
                }

                State.SKIP_LOCATION -> {
                    cleanup(bot, chatId, user, getLocalisedMessage("search_in_progress", user.languageCode))
                    onSearchRequestCreated(user, requestDraft)
                    return
                }
            }
        }
    }

    private fun cleanup(bot: Bot, chatId: ChatId, user: User, message: String) {
        bot.clearKeyboard(chatId, message)
        userStates.remove(user.id)
        drafts.remove(user.id)
    }

    private fun checkValidLocation(message: Message?): Boolean {
        return message?.location?.let { true } ?: false
    }

    private fun checkValidRadius(message: Message?): Boolean {
        return message?.text?.toDoubleOrNull()?.let { true } ?: false
    }

    private fun checkSkipLocation(message: Message?, skipMessage: String): Boolean {
        return message?.text == skipMessage
    }

    private fun requestLocation(
        bot: Bot,
        user: User,
        chatId: ChatId.Id,
        cancelButton: KeyboardButton,
        skipButton: KeyboardButton,
    ) {
        val shareLocationButton =
            KeyboardButton(getLocalisedMessage("make_wish_location_share", user.languageCode), requestLocation = true)
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("make_wish_request_location", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(
                listOf(listOf(shareLocationButton, skipButton, cancelButton)),
                resizeKeyboard = true
            )
        )
        userStates[user.id] = State.WAIT_FOR_LOCATION
    }

    private fun requestRadius(bot: Bot, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton) {
        bot.sendMessage(
            chatId = chatId,
            text = getLocalisedMessage("make_wish_request_radius", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
        userStates[user.id] = State.WAIT_FOR_RADIUS
    }

    private fun isLocationStageDone(
        bot: Bot,
        requestDraft: SearchInfoDraft,
        message: Message?,
        user: User,
        chatId: ChatId.Id,
        cancelButton: KeyboardButton,
    ): Boolean {
        val skipMessage = getLocalisedMessage("search_location_skip", user.languageCode)
        val skipButton = KeyboardButton(skipMessage)
        val answer = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestLocation(bot, user, chatId, cancelButton, skipButton) },
            checkValidText = { (userStates[user.id] == State.WAIT_FOR_LOCATION) && (checkValidLocation(it) || checkSkipLocation(it, skipMessage)) },
        ) ?: return false
        if (checkSkipLocation(answer, skipMessage)) {
            userStates[user.id] = State.SKIP_LOCATION
        }
        else {
            val location = answer.location!!
            userStates[user.id] = State.ASK_FOR_RADIUS
            requestDraft.location = we.rashchenko.patronum.search.geo.Location(location.longitude, location.latitude)
        }
        return true
    }

    private fun isRadiusStageDone(
        bot: Bot,
        requestDraft: SearchInfoDraft,
        message: Message?,
        user: User,
        chatId: ChatId.Id,
        cancelButton: KeyboardButton,
    ): Boolean {
        val radius = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestRadius(bot, user, chatId, cancelButton) },
            checkValidText = { (userStates[user.id] == State.WAIT_FOR_RADIUS) && checkValidRadius(it) }
        )?.text ?: return false
        requestDraft.radius = radius.toFloat()
        bot.sendMessage(chatId, getLocalisedMessage("search_start", user.languageCode))
        return true
    }
}