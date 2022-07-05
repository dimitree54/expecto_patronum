package we.rashchenko.patronum

import com.mongodb.client.model.geojson.Polygon
import org.bson.types.ObjectId
import java.util.*

class Wish(
    val authorId: ObjectId,
    val id: ObjectId = ObjectId.get()
) {
    var title: String? = null
    var description: String? = null
    var image: String? = null
    val tagIds: MutableList<ObjectId> = mutableListOf()
    var wishArea: Polygon? = null
    var expirationDate: Date? = null

    var patronId: String? = null
}