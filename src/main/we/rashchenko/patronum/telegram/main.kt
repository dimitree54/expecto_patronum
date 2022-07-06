package we.rashchenko.patronum.telegram


fun main() {
    val bot = ExpectoPatronum().build()
    bot.startPolling()
}
