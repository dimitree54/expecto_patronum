package we.rashchenko.patronum.database

import we.rashchenko.patronum.database.stats.UserStats

class PatronUser(val id: String, val telegramId: Long, val stats: UserStats) {
    val wishIdBlackList = mutableListOf<String>()
    val authorIdBlackList = mutableListOf<String>()
}