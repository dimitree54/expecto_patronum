package we.rashchenko.patronum.database.stats

import we.rashchenko.patronum.database.PatronUser

class Report(
    val id: String,
    val sender: PatronUser,
    val receiver: PatronUser,
    val message: String
)