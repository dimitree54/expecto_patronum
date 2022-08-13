package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.SearchEngine
import we.rashchenko.patronum.errors.UserNotExistError
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.Wish

class MongoSearchEngine(
    private val wishesCollection: MongoCollection<Wish>, private val usersCollection: MongoCollection<PatronUser>
) : SearchEngine {
    override fun search(patron: PatronUser, query: SearchInfo): Iterable<Wish> {
        val pipeline = mutableListOf<Bson>()
        query.searchArea?.let {
            pipeline.add(Aggregates.match(Filters.geoIntersects("searchPolygon", it.toMongo())))
        } ?: run {
            pipeline.add(Aggregates.match(Filters.not(Filters.exists("searchPolygon"))))
        }
        pipeline.add(Aggregates.match(Filters.not(Filters.exists("patronId"))))
        pipeline.add(Aggregates.match(Filters.eq("closed", false)))
        pipeline.add(Aggregates.match(Filters.nin("_id", patron.wishIdStopList.map{ ObjectId(it) })))
        pipeline.add(Aggregates.match(Filters.nin("authorId", patron.userIdStopList.map{ ObjectId(it) })))
        pipeline.add(Aggregates.match(Filters.not(Filters.eq("authorId", ObjectId(patron.id)))))
        pipeline.add(Aggregates.lookup("users", "authorId", "_id", "author"))
        pipeline.add(Aggregates.sort(Sorts.descending("author.0.statsReputation")))

        return wishesCollection.aggregate(pipeline).filter { wish ->
            val author = usersCollection.find(Filters.eq("_id", ObjectId(wish.authorId))).firstOrNull() ?: throw UserNotExistError()
            patron.id !in author.userIdStopList
        }
    }
}