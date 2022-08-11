package we.rashchenko.patronum.wishes

import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title
import java.time.Instant

class Wish(
    val id: String,
    val authorId: String,
    val title: Title,
    val description: Description,
    val searchInfo: SearchInfo,
    val creationDate: Instant,
    var expirationDate: Instant? = null,
    var patronId: String? = null,
    var bounty: Int = 0,
    var closed: Boolean = false
)