package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import org.bson.conversions.Bson
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
            pipeline.add(Aggregates.match(Filters.geoIntersects("wishArea", it.toMongo())))
        }
        pipeline.add(Aggregates.match(Filters.not(Filters.exists("patron"))))
        pipeline.add(Aggregates.match(Filters.nin("_id", patron.wishIdBlackList)))
        pipeline.add(Aggregates.lookup("users", "author_id", "_id", "author"))
        pipeline.add(Aggregates.match(Filters.nin("author._id", patron.authorIdBlackList)))
        pipeline.add(Aggregates.match(Filters.not(Filters.eq("author._id", patron.id))))
        pipeline.add(Aggregates.sort(Sorts.descending("author.reputation")))

        return wishesCollection.aggregate(pipeline)
    }

    private fun Location.toMongo(): com.mongodb.client.model.geojson.Position{
        return com.mongodb.client.model.geojson.Position(latitude.toDouble(), longitude.toDouble())
    }

    private fun Polygon.toMongo(): com.mongodb.client.model.geojson.Polygon{
        return com.mongodb.client.model.geojson.Polygon(
            this.points.map { it.toMongo() }
        )
    }
}