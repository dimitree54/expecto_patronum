package we.rashchenko.patronum.ui.telegram.hotel


fun main() {
    val moderatorUserId = 5326457106L
    val hotel = TelegramHotel(moderatorUserId)
    hotel.openRoom("test", listOf(530993342L))
}