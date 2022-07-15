package we.rashchenko.patronum

import com.github.kotlintelegrambot.entities.Location
import com.mongodb.client.model.geojson.Polygon
import org.bson.types.ObjectId

class SearchRequest {
    var author: User? = null
    var tagIds: List<ObjectId>? = null
    var location: Location? = null
    var radius: Double = 0.0
    private var _wishArea: Polygon? = null
    var searchArea: Polygon?
        get() = _wishArea ?: location?.let { circle2polygon(it.longitude.toDouble(), it.latitude.toDouble(), radius) }
        set(value) {
            _wishArea = value
        }
}