package we.rashchenko.patronum.ui.telegram.hotel

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi
import we.rashchenko.patronum.errors.RoomOpenError

class TelegramHotel(private val moderatorUserId: Long) {
    private val client: SimpleTelegramClient
    private val retries = 5

    init {
        Init.start()
        val apiId = System.getenv("TELEGRAM_API_ID").toInt()
        val apiHash = System.getenv("TELEGRAM_API_HASH")
        val apiToken = APIToken(apiId, apiHash)
        val settings = TDLibSettings.create(apiToken)
        client = SimpleTelegramClient(settings)
        start()
    }

    private enum class HotelState{
        OPEN, OPENING, CLOSED, ERROR
    }

    private fun start() {
        var state = HotelState.CLOSED
        // Configure the client
        client.addUpdateHandler(TdApi.UpdateAuthorizationState::class.java) {
            state = when (it.authorizationState) {
                is TdApi.AuthorizationStateReady -> HotelState.OPEN
                is TdApi.AuthorizationStateWaitTdlibParameters,
                is TdApi.AuthorizationStateWaitEncryptionKey -> HotelState.OPENING
                else -> HotelState.ERROR
            }
        }

        // Start the client
        val authenticationData = AuthenticationData.consoleLogin()
        client.start(authenticationData)

        repeat(retries) {
            if (state == HotelState.OPEN) {
                println("Hotel opened")
                return
            }
            if (state == HotelState.ERROR) {
                throw IllegalStateException("Hotel opening failed")
            }
            Thread.sleep(1000)
        }
    }

    private fun leaveRoom(chatId: Long) {
        client.send(TdApi.LeaveChat(chatId)) {
            println("Chat left")
        }
    }

    private fun sendStartMessage(chatId: Long, onSuccess: () -> Unit) {
        client.send(
            TdApi.SendMessage(
                chatId,
                0,
                0,
                TdApi.MessageSendOptions(
                    true,
                    false,
                    false,
                    null
                ),
                null,
                TdApi.InputMessageText(
                    TdApi.FormattedText(
                        "/start",
                        emptyArray()
                    ),
                    true,
                    true
                )
            )
        ) {
            println("Start message sent")
            onSuccess()
        }
    }

    fun openRoom(title: String, userIds: List<Long>, onRoomOpened: (Long) -> Unit): Long {
        println("Received open room command. opening room...")
        var chatId: Long? = null
        client.send(
            TdApi.CreateNewBasicGroupChat(
                (userIds + moderatorUserId).toLongArray(), title
            )
        ) { resultChat ->
            val chat = resultChat.get()
            chatId = chat.id
            onRoomOpened(chatId!!)
            sendStartMessage(chat.id) {
                leaveRoom(chat.id)
            }
            println("Created chat ${chat.id}")
        }
        repeat(retries) {
            if (chatId != null) {
                return chatId!!
            }
            println("Waiting for room to be opened...")
            Thread.sleep(1000)
        }
        throw RoomOpenError("Can not open error")
    }
}
