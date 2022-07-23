package we.rashchenko.patronum.database

import we.rashchenko.patronum.database.stats.Report
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.Wish

class Database(
    private val users: UsersDatabase,
    private val wishes: WishesDatabase,
    private val reports: ReportsDatabase,
    private val searchEngine: SearchEngine,
    private val rooms: RoomsDatabase
) {
    fun newUser(user: PatronUser) = users.new(user)
    fun getUserByTelegramId(telegramId: Long) = users.getByTelegramId(telegramId)
    fun getGlobalStats() = users.getGlobalStats()

    fun registerReport(report: Report) {
        reports.new(report)
        report.receiver.stats.receiveReport()
        report.sender.stats.sendReport()
        report.sender.authorIdBlackList.add(report.receiver.id)
        report.receiver.authorIdBlackList.add(report.sender.id)
        users.update(report.sender)
        users.update(report.receiver)
    }

    fun newWish(wish: Wish){
        wish.author.stats.myNewWish()
        wish.author.stats.stakeBounty(wish.bounty)
        users.update(wish.author)
        wishes.new(wish)
    }
    fun getWishesByAuthor(user: PatronUser) = wishes.getByAuthor(user)
    fun getWishesByPatron(user: PatronUser) = wishes.getByPatron(user)
    fun cancelWishByAuthor(wish: Wish){
        wish.author.stats.myWishCancel()
        users.update(wish.author)
        wishes.cancel(wish)
    }
    fun cancelWishByPatron(wish: Wish){
        val patron = wish.patron!!
        patron.stats.othersWishCancel()
        users.update(patron)
        wish.patron = null
        wishes.update(wish)
    }
    fun finishWish(wish: Wish, rate: Float = 1f){
        val patron = wish.patron!!
        wish.author.stats.myWishDone()
        patron.stats.othersWishDone(rate, wish.bounty)
        users.update(wish.author)
        users.update(patron)
        wishes.cancel(wish)
    }
    fun acceptWish(patron: PatronUser, wish: Wish){
        patron.wishIdBlackList.add(wish.id)
        users.update(patron)
        wish.patron = patron
        wishes.update(wish)
    }
    fun skipWish(patron: PatronUser, wish: Wish){
        patron.wishIdBlackList.add(wish.id)
        users.update(patron)
    }
    fun search(patron: PatronUser, query: SearchInfo) = searchEngine.search(patron, query)

    fun getRoomByTelegramId(telegramId: Long) = rooms.getByTelegramId(telegramId)
    fun openWishRoom(wishRoom: WishRoom) = rooms.new(wishRoom)

    fun generateNewUserId() = users.generateNewUserId()
    fun generateNewWishId() = wishes.generateNewWishId()
    fun generateNewReportId() = reports.generateNewReportId()
    fun generateNewRoomId() = rooms.generateNewReportId()
}