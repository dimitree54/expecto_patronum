package we.rashchenko.patronum.hotel

import it.tdlight.client.*
import it.tdlight.common.Init
import it.tdlight.jni.TdApi

val ADMIN_ID = TdApi.MessageSenderUser(2057125477)

fun main() {
    Init.start()

    // Obtain the API token
    val apiId = System.getenv("TELEGRAM_API_ID").toInt()
    val apiHash = System.getenv("TELEGRAM_API_HASH")
    val apiToken = APIToken(apiId, apiHash)

    // Configure the client
    val settings = TDLibSettings.create(apiToken)
    val client = SimpleTelegramClient(settings)
    client.addUpdateHandler(TdApi.UpdateAuthorizationState::class.java){
        when(it.authorizationState){
            is TdApi.AuthorizationStateReady -> println("Logged in")
            is TdApi.AuthorizationStateClosing -> println("Closing...")
            is TdApi.AuthorizationStateClosed -> println("Closed")
            is TdApi.AuthorizationStateLoggingOut -> println("Logging out...")
            else -> println("Unknown state")
        }
    }
    client.addUpdateHandler(TdApi.UpdateNewMessage::class.java){
        // Get the message content
        val messageContent = it.message.content

        // Get the message text
        val text = if (messageContent is TdApi.MessageText) {
            // Get the text of the text message
            messageContent.text.text
        } else {
            // We handle only text messages, the other messages will be printed as their type
            String.format("(%s)", messageContent::class.java.simpleName)
        }

        // Get the chat title
        client.send(TdApi.GetChat(it.message.chatId)) { chatIdResult ->
            // Get the chat response
            val chat = chatIdResult.get()
            // Get the chat name
            val chatName = chat.title

            // Print the message
            println("Received new message from chat $chatName: $text")
        }
    }

    // Add an example command handler that stops the bot
    client.addCommandHandler<TdApi.Update>("stop", StopCommandHandler(client))

    // Start the client
    val authenticationData = AuthenticationData.consoleLogin()
    client.start(authenticationData)

    // Wait for exit
    client.waitForExit()
}

/**
 * Close the bot if the /stop command is sent by the administrator
 */
class StopCommandHandler(private val client: SimpleTelegramClient) : CommandHandler {

    override fun onCommand(chat: TdApi.Chat, commandSender: TdApi.MessageSender, arguments: String) {
        // Check if the sender is the admin
        if (isAdmin(commandSender)) {
            // Stop the client
            println("Received stop command. closing...")
            client.sendClose()
        }
    }
}

/**
 * Check if the command sender is admin
 */
fun isAdmin(sender: TdApi.MessageSender): Boolean {
    return sender == ADMIN_ID
}