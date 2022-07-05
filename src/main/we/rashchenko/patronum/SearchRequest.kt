package we.rashchenko.patronum

import com.mongodb.client.model.geojson.Polygon
import org.bson.types.ObjectId

class SearchRequest(val author: User) {
    var tagIds: List<ObjectId>? = null
    var searchArea: Polygon? = null
}