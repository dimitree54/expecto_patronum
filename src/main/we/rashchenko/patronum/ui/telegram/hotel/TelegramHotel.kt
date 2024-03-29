package we.rashchenko.patronum.ui.telegram.hotel

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi
import we.rashchenko.patronum.errors.RoomOpenError
import we.rashchenko.patronum.ui.messages.getLocalisedMessage

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

    private fun sendMessage(chatId: Long, messageText: String, onSuccess: () -> Unit) {
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
                        messageText,
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

    fun checkInUser(userId: Long): Boolean{
        println("Registering user $userId in hotel")
        var userRegistered = false
        val languageCode = getUser(userId)?.languageCode ?: return false
        val checkInMessage = getLocalisedMessage("hotel_check_in", languageCode)
        client.send(TdApi.CreatePrivateChat(userId, false)) {
            println("Private chat created")
            val chatId = it.get().id
            sendMessage(chatId,
                checkInMessage) {
                userRegistered = true
            }
        }
        var i = 0
        while (!userRegistered && i < retries) {
            Thread.sleep(1000)
            i++
        }
        return userRegistered
    }

    private fun getUser(userId: Long): TdApi.User? {
        var user: TdApi.User? = null
        client.send(TdApi.GetUser(userId)) {
            println("User $userId exist")
            user = it.get()
        }
        var i = 0
        while (user == null && i < retries) {
            Thread.sleep(1000)
            i++
        }
        return user
    }

    fun checkUserAvailable(userId: Long): Boolean {
        println("Checking user $userId")
        getUser(userId)?.let {
            print("User $userId is available")
            return true
        }
        return false
    }

    private fun checkAllUsersAvailable(userIds: List<Long>): Boolean{
        println("Retrieving users info...")
        userIds.all{checkUserAvailable(it)}
        println("All users exist and reachable")
        return true
    }

    fun openRoom(title: String, userIds: List<Long>, onRoomOpened: (Long) -> Unit): Long {
        println("Received open room command. opening room...")
        var chatId: Long? = null

        if (!checkAllUsersAvailable(userIds)){
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
            sendMessage(chat.id, "/start") {
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
