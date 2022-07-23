package we.rashchenko.patronum.ui.telegram.hotel.moderator

import we.rashchenko.patronum.database.mongo.MongoDatabaseBuilder


fun main() {
    val database = MongoDatabaseBuilder().build()
    val bot = ModeratorBot(database).build()
    println("Moderator bot is running. Id: ${bot.getMe().get().id}")
    bot.startPolling()
}
