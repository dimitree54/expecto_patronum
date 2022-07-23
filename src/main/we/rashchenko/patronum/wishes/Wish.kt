package we.rashchenko.patronum.wishes

import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title
import java.time.Instant

class Wish(
    val id: String,
    val author: PatronUser,
    val title: Title,
    val description: Description,
    val searchInfo: SearchInfo,
    val creationDate: Instant,
    val expirationDate: Instant,
    var patron: PatronUser? = null,
    var bounty: Float = 0f,
)