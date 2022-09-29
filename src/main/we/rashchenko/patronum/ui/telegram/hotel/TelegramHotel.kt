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
        OPEN, OPENING, CLOSED, ERROR, WAIT_AUTH
    }

    private fun start() {
        var state = HotelState.CLOSED
        // Configure the client
        client.addUpdateHandler(TdApi.UpdateAuthorizationState::class.java) {
            state = when (it.authorizationState) {
                is TdApi.AuthorizationStateReady -> HotelState.OPEN
                is TdApi.AuthorizationStateWaitTdlibParameters,
                is TdApi.AuthorizationStateWaitEncryptionKey -> HotelState.OPENING
                is TdApi.AuthorizationStateWaitPhoneNumber -> HotelState.WAIT_AUTH
                is TdApi.AuthorizationStateWaitCode -> HotelState.WAIT_AUTH
                else -> {
                    println("Unknown authorization state: ${it.authorizationState}")
                    HotelState.ERROR
                }
            }
        }

        // Start the client
        val authenticationData = AuthenticationData.consoleLogin()
        client.start(authenticationData)

        var i = 0
        while (state != HotelState.OPEN && i < retries) {
            when (state) {
                HotelState.OPEN -> {
                    println("Hotel opened")
                    return
                }
                HotelState.ERROR -> {
                    throw IllegalStateException("Hotel opening failed")
                }
                HotelState.WAIT_AUTH -> {
                }
                else -> {
                    i++
                }
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

    private fun checkAllUsersExist(userIds: List<Long>): Boolean{
        println("Retrieving users info...")
        userIds.forEach { userId ->
            var userExist = false
            client.send(TdApi.GetUser(userId)) {
                userExist = true
            }
            var i = 0
            while (!userExist && i < retries) {
                Thread.sleep(1000)
                i++
            }
            if (!userExist) {
                return false
            }
        }
        println("All users exist and reachable")
        return true
    }

    fun openRoom(title: String, userIds: List<Long>, onRoomOpened: (Long) -> Unit): Long {
        println("Received open room command. opening room...")
        var chatId: Long? = null

        if (!checkAllUsersExist(userIds)){
            throw RoomOpenError("Some users are not found")
        }

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
