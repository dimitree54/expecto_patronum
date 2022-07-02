package we.rashchenko

import java.util.*

class Wish(
    val id: Int,
    val author: User
) {
    var title: String? = null
    var description: String? = null
    var image: String? = null
    var tags: List<Tag>? = null
    var location: Location? = null
    var expirationDate: Date? = null

    val dateCreated: Date = Date()
    val dateClosed: Date? = null

    var completed: Boolean = false
    var active: Boolean = false
    var inProgress: Boolean = false
    var fulfiller: User? = null
}