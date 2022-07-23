package we.rashchenko.patronum.database

import we.rashchenko.patronum.hotel.WishRoom

interface RoomsDatabase {
    fun getByTelegramId(telegramId: Long): WishRoom?
    fun new(room: WishRoom)
    fun update(room: WishRoom)
    fun getOpen(): Iterable<WishRoom>
    fun generateNewReportId(): String
}