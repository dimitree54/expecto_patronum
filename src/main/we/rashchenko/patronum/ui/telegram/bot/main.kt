package we.rashchenko.patronum.ui.telegram.bot


fun main() {
    val bot = ExpectoPatronum().build()
    println("Bot is running")
    bot.startPolling()
}
