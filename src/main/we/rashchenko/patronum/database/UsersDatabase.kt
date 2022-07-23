package we.rashchenko.patronum.database

import we.rashchenko.patronum.database.stats.GlobalStats

interface UsersDatabase {
    fun new(user: PatronUser)
    fun get(id: String): PatronUser?
    fun getByTelegramId(telegramId: Long): PatronUser?
    fun update(user: PatronUser)
    fun getGlobalStats(): GlobalStats
    fun generateNewUserId(): String
}