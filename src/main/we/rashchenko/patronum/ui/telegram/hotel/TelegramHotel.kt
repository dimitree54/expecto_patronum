package we.rashchenko.patronum.ui.telegram.hotel

import it.tdlight.client.APIToken
import it.tdlight.client.AuthenticationData
import it.tdlight.client.SimpleTelegramClient
import it.tdlight.client.TDLibSettings
import it.tdlight.common.Init
import it.tdlight.jni.TdApi

class TelegramHotel(private val moderatorUserId: Long) {
    private val client: SimpleTelegramClient

    init {
        Init.start()
        val apiId = System.getenv("TELEGRAM_API_ID").toInt()
        val apiHash = System.getenv("TELEGRAM_API_HASH")
        val apiToken = APIToken(apiId, apiHash)
        val settings = TDLibSettings.create(apiToken)
        client = SimpleTelegramClient(settings)
        start()
    }

    private fun start() {
        var ready = false
        // Configure the client
        client.addUpdateHandler(TdApi.UpdateAuthorizationState::class.java) {
            when (it.authorizationState) {
                is TdApi.AuthorizationStateReady -> {
                    println("Logged in")
                    ready = true
                }
                is TdApi.AuthorizationStateClosing -> println("Closing...")
                is TdApi.AuthorizationStateClosed -> println("Closed")
                is TdApi.AuthorizationStateLoggingOut -> println("Logging out...")
                else -> println("Unknown authorisation state: $it")
            }
        }

        // Start the client
        val authenticationData = AuthenticationData.consoleLogin()
        client.start(authenticationData)
        while (!ready) {
            println("Waiting for login...")
            Thread.sleep(1000)
        }
    }

    private fun leaveRoom(chatId: Long) {
        client.send(TdApi.LeaveChat(chatId)) {
            println("Status of leaving chat: ${it.get()}")
        }
    }

    private fun sendStartMessage(chatId: Long, onSuccess: () -> Unit){
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
        ){
            println("Status of sending start message: ${it.get()}")
            onSuccess()
        }
    }

    fun openRoom(title: String, userIds: List<Long>): Long {
        println("Received open room command. opening room...")
        var chatId: Long? = null
        client.send(
            TdApi.CreateNewBasicGroupChat(
                (userIds + moderatorUserId).toLongArray(), title
            )
        ) { resultChat ->
            val chat = resultChat.get()
            chatId = chat.id
            sendStartMessage(chat.id){
                leaveRoom(chat.id)
            }
            println("Created chat ${chat.id}")
        }
        while (chatId == null) {
            println("Waiting for room to be opened...")
            Thread.sleep(1000)
        }
        return chatId!!
    }
}
