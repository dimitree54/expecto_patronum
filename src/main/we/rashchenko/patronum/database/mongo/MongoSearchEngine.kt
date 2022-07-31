package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.SearchEngine
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.search.geo.Location
import we.rashchenko.patronum.search.geo.Polygon
import we.rashchenko.patronum.wishes.Wish

class MongoSearchEngine(
    private val wishesCollection: MongoCollection<Wish>
) : SearchEngine {
    override fun search(patron: PatronUser, query: SearchInfo): Iterable<Wish> {
        val pipeline = mutableListOf<Bson>()
        query.searchArea?.let {
            pipeline.add(Aggregates.match(Filters.geoIntersects("search_polygon", it.toMongo())))
        } ?: run {
            pipeline.add(Aggregates.match(Filters.not(Filters.exists("search.polygon"))))
        }
        pipeline.add(Aggregates.match(Filters.not(Filters.exists("patronId"))))
        pipeline.add(Aggregates.match(Filters.nin("_id", patron.wishIdBlackList.map{ ObjectId(it) })))
        pipeline.add(Aggregates.lookup("users", "authorId", "_id", "author"))
        pipeline.add(Aggregates.match(Filters.nin("author._id", patron.authorIdBlackList.map{ ObjectId(it) })))
        pipeline.add(Aggregates.match(Filters.not(Filters.eq("author._id", ObjectId(patron.id)))))
        pipeline.add(Aggregates.sort(Sorts.descending("author.reputation")))

        return wishesCollection.aggregate(pipeline)
    }
}