package we.rashchenko.patronum.telegram


fun main() {
    val bot = ExpectoPatronum().build()
    println("Bot is running")
    bot.startPolling()
}
