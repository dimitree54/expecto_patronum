package we.rashchenko.patronum

import com.github.kotlintelegrambot.entities.Location
import com.mongodb.client.model.geojson.Polygon
import org.bson.types.ObjectId
import java.util.*

class Wish(
    val id: ObjectId = ObjectId.get()
) {
    var authorId: ObjectId? = null
    var title: String? = null
    var description: String? = null
    var image: String? = null
    val tagIds: MutableList<ObjectId> = mutableListOf()

    var location: Location? = null
    var radius: Double = 0.0
    private var _wishArea: Polygon? = null
    var wishArea: Polygon?
        get() = _wishArea ?: location?.let { circle2polygon(it.longitude.toDouble(), it.latitude.toDouble(), radius) }
        set(value) {
            _wishArea = value
        }
    var expirationDate: Date? = null

    var patronId: String? = null
}