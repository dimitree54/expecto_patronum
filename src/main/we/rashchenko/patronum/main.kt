package we.rashchenko.patronum

import we.rashchenko.patronum.database.mongo.MongoDatabaseBuilder
import we.rashchenko.patronum.ui.telegram.bot.ExpectoPatronum
import we.rashchenko.patronum.ui.telegram.hotel.moderator.ModeratorBot
import kotlin.concurrent.thread

fun main() {
    thread {
        val database = MongoDatabaseBuilder().build()
        val bot = ModeratorBot(database).build()
        println("Moderator bot is running. Id: ${bot.getMe().get().id}")
        bot.startPolling()
    }
    thread {
        val bot = ExpectoPatronum().build()
        println("Main bot is running")
        bot.startPolling()
    }
}