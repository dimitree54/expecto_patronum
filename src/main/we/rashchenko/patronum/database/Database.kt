package we.rashchenko.patronum.database

import we.rashchenko.patronum.database.stats.Report
import we.rashchenko.patronum.database.stats.UserStats
import we.rashchenko.patronum.errors.*
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.wishes.WishDraft
import java.time.Period
import java.util.*

class Database(
    private val users: UsersDatabase,
    private val wishes: WishesDatabase,
    private val reports: ReportsDatabase,
    private val searchEngine: SearchEngine,
    private val rooms: RoomsDatabase,
) {

    private val newUserReputation = Properties().let {
        it.load(ClassLoader.getSystemResourceAsStream("reputation.properties"))
        it.getProperty("start").toInt()
    }

    private val expirationPeriod: Period
    private val closeWishPeriod: Period

    init {
        Properties().let {
            it.load(ClassLoader.getSystemResourceAsStream("limits.properties"))
            expirationPeriod = Period.ofDays(it.getProperty("wishExpirationDays").toInt())
            closeWishPeriod = Period.ofDays(it.getProperty("wishRemoveDelayDays").toInt())
        }
    }

    fun newUser(telegramId: Long, languageCode: String?) {
        val newUser = PatronUser(generateNewUserId(), telegramId, UserStats(newUserReputation))
        newUser.languageCode = languageCode
        users.new(newUser)
    }

    fun getUserById(id: String): PatronUser = users.get(id) ?: throw UserNotExistError()
    fun getUserByTelegramId(telegramId: Long): PatronUser =
        users.getByTelegramId(telegramId) ?: throw UserNotExistError()

    fun getGlobalStats() = users.getGlobalStats()

    private fun registerReport(report: Report) {
        reports.new(report)
        report.receiver.stats.receiveReport()
        report.sender.stats.sendReport()
        report.sender.userIdStopList.add(report.receiver.id)
        report.receiver.userIdStopList.add(report.sender.id)
        users.update(report.sender)
        users.update(report.receiver)
    }

    fun registerRoomReport(requestFromTelegramId: Long, room: WishRoom) {
        val wish = getWishById(room.wishId)
        val author = getUserById(wish.authorId)
        val patron = getUserById(wish.patronId ?: throw PatronNotExistError())
        when (requestFromTelegramId) {
            author.telegramId -> {
                if (room.reportedByAuthor) throw ReportAgainError()
                registerReport(Report(generateNewReportId(), author, patron, ""))
                room.reportedByAuthor = true
            }

            patron.telegramId -> {
                if (room.reportedByPatron) throw ReportAgainError()
                registerReport(Report(generateNewReportId(), patron, author, ""))
                room.reportedByPatron = true
            }

            else -> throw InvalidWishRightsError()
        }
        updateRoom(room)
    }

    fun newWish(authorTelegramId: Long, wishDraft: WishDraft) {
        val author = getUserByTelegramId(authorTelegramId)
        val wish = wishDraft.toWish(generateNewWishId(), author.id, expirationPeriod)

        wishes.new(wish)
        author.stats.myNewWish()
        author.stats.stakeBounty(wish.bounty)
        users.update(author)
    }

    fun getWishById(wishId: String): Wish = wishes.get(wishId) ?: throw WishNotExistError()
    fun getWishesByAuthor(user: PatronUser) = wishes.getByAuthor(user)
    fun getWishesByPatron(user: PatronUser) = wishes.getByPatron(user)
    fun cancelWishByAuthor(wish: Wish) {
        val author = getUserById(wish.authorId)
        author.stats.myWishCancel()
        users.update(author)
        wishes.cancel(wish)
    }

    private fun cancelFulfillmentByAuthor(wish: Wish) {
        val author = getUserById(wish.authorId)
        author.stats.myFulfillmentCancel()
        users.update(author)
        wish.patronId = null
        wishes.update(wish)
    }

    fun cancelFulfillmentByPatron(wish: Wish) {
        val patron = getUserById(wish.patronId ?: throw PatronNotExistError())
        patron.stats.othersFulfillmentCancel()
        users.update(patron)
        wish.patronId = null
        wishes.update(wish)
    }

    private fun finishWish(wish: Wish, rate: Float = 1f) {
        val author = getUserById(wish.authorId)
        val patron = getUserById(wish.patronId ?: throw PatronNotExistError())
        author.stats.myWishDone()
        patron.stats.othersWishDone(rate, wish.bounty)
        users.update(author)
        users.update(patron)
        wishes.cancel(wish)
    }

    fun acceptWish(patron: PatronUser, wish: Wish) {
        patron.wishIdStopList.add(wish.id)
        users.update(patron)
        wish.patronId = patron.id
        wishes.update(wish)
    }

    fun skipWish(patron: PatronUser, wish: Wish) {
        patron.wishIdStopList.add(wish.id)
        users.update(patron)
    }

    fun search(patron: PatronUser, query: SearchInfo) = searchEngine.search(patron, query)

    fun getRoomByTelegramId(telegramId: Long) = rooms.getByTelegramId(telegramId) ?: throw RoomNotExistError()
    private fun updateRoom(room: WishRoom) = rooms.update(room)
    fun openWishRoom(roomTelegramId: Long, wish: Wish) {
        val author = getUserById(wish.authorId)
        val patron = getUserById(wish.patronId ?: throw PatronNotExistError())
        rooms.new(
            WishRoom(generateNewRoomId(), roomTelegramId, wish.id).apply {
                author.languageCode?.let { addLanguageCode(it) }
                patron.languageCode?.let { addLanguageCode(it) }
            })
    }

    fun finishRoomWish(requestFromTelegramId: Long, wishRoom: WishRoom) {
        val wish = getWishById(wishRoom.wishId)
        val requestFromId = getUserByTelegramId(requestFromTelegramId).id
        if (requestFromId != wish.authorId) throw InvalidWishRightsError()
        if (wishRoom.closed) throw RoomClosedError()
        if (wish.closed) throw WishClosedError()
        wishRoom.finished = true
        finishWish(wish)
        updateRoom(wishRoom)
    }

    fun cancelRoomWish(requestFromTelegramId: Long, wishRoom: WishRoom) {
        val wish = getWishById(wishRoom.wishId)
        val requestFromId = getUserByTelegramId(requestFromTelegramId).id
        if (wishRoom.closed) throw RoomClosedError()
        if (wish.closed) throw WishClosedError()
        when (requestFromId) {
            wish.authorId -> {
                wishRoom.canceledByAuthor = true
                cancelFulfillmentByAuthor(wish)
            }

            wish.patronId -> {
                wishRoom.canceledByPatron = true
                cancelFulfillmentByPatron(wish)
            }

            else -> throw InvalidWishRightsError()
        }
        updateRoom(wishRoom)
    }

    private fun generateNewUserId() = users.generateNewUserId()
    private fun generateNewWishId() = wishes.generateNewWishId()
    private fun generateNewReportId() = reports.generateNewReportId()
    private fun generateNewRoomId() = rooms.generateNewReportId()
}