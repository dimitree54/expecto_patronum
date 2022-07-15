package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import we.rashchenko.patronum.SearchRequest
import we.rashchenko.patronum.getLocalisedMessage


class SearchHandler(
    private val externalCheckUpdate: (Long) -> Boolean,
    private val onSearchRequestCreated: (Long, SearchRequest) -> Unit,
    private val onCancel: (Long) -> Unit,
) : Handler {

    private enum class MakeARequestState {
        ASK_FOR_LOCATION, WAIT_FOR_LOCATION, ASK_FOR_RADIUS, WAIT_FOR_RADIUS
    }

    private val userStates = mutableMapOf<Long, MakeARequestState>()
    private val drafts = mutableMapOf<Long, SearchRequest>()
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

        val requestDraft = drafts.getOrPut(user.id) { SearchRequest() }
        while (true) {
            when (userStates.getOrPut(user.id) { MakeARequestState.ASK_FOR_LOCATION }) {
                MakeARequestState.ASK_FOR_LOCATION, MakeARequestState.WAIT_FOR_LOCATION -> {
                    if (isLocationStageDone(bot, requestDraft, message, user, chatId, cancelButton)) continue else break
                }
                MakeARequestState.ASK_FOR_RADIUS, MakeARequestState.WAIT_FOR_RADIUS -> {
                    if (isRadiusStageDone(bot, requestDraft, message, user, chatId, cancelButton)) {
                        onSearchRequestCreated(user.id, requestDraft)
                        return
                    }
                    else break
                }
            }
        }
    }

    private fun checkValidLocation(message: Message?): Boolean {
        return message?.location?.let { true } ?: false
    }

    private fun checkValidRadius(message: Message?): Boolean {
        return message?.text?.toDoubleOrNull()?.let { true } ?: false
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
            chatId = chatId, text = getLocalisedMessage("request_radius", user.languageCode),
            replyMarkup = KeyboardReplyMarkup(listOf(listOf(cancelButton)), resizeKeyboard = true)
        )
    }


    private fun isLocationStageDone(
        bot: Bot, requestDraft: SearchRequest, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val location = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestLocation(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidLocation
        )?.location
        return if (location != null) {
            userStates[user.id] = MakeARequestState.ASK_FOR_RADIUS
            requestDraft.location = location
            true
        } else {
            false
        }
    }

    private fun isRadiusStageDone(
        bot: Bot, requestDraft: SearchRequest, message: Message?, user: User, chatId: ChatId.Id, cancelButton: KeyboardButton
    ): Boolean {
        val radius = askAndWaitForAnswer(
            message,
            sendRequestMessage = { requestRadius(bot, user, chatId, cancelButton) },
            checkValidText = ::checkValidRadius
        )?.text
        return if (radius != null) {
            requestDraft.radius = radius.toDouble()
            bot.sendMessage(chatId, getLocalisedMessage("request_confirmation_search_done", user.languageCode))
            true
        } else {
            false
        }
    }
}