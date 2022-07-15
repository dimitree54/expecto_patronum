package we.rashchenko.patronum.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromCodecs
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import we.rashchenko.patronum.*

class Database {
    private val mongoClient: MongoClient
    private val database: MongoDatabase
    private val usersCollection: MongoCollection<User>
    private val wishesCollection: MongoCollection<Wish>
    private val tagsCollection: MongoCollection<Tag>

    private val scoreReportPenalty: Double
    private val scoreCancelPenalty: Double
    private val scoreRefusePenalty: Double
    private val scoreFinishPatronBonusMax: Double
    private val scoreFinishReceiverPenalty: Double
    private val scoreNewWishPenalty: Double

    init {
        val properties = java.util.Properties()
        properties.load(ClassLoader.getSystemResourceAsStream("application.properties"))
        val protocol = properties["mongodb.protocol"] as String
        val host = properties["mongodb.host"] as String
        val user = System.getenv("MONGODB_USER")
        val password = System.getenv("MONGODB_PASSWORD")
        val connectionString = ConnectionString("$protocol://$user:$password@$host")
        val settings: MongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build()
        mongoClient = MongoClients.create(settings)

        val databaseName = properties["mongodb.database"] as String
        database = mongoClient.getDatabase(databaseName)

        usersCollection = database.getCollection("users", User::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(UserCodec())
            )
        )
        wishesCollection = database.getCollection("wishes", Wish::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(WishCodec())
            )
        )
        tagsCollection = database.getCollection("tags", Tag::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(TagCodec())
            )
        )

        scoreReportPenalty = (properties["score.report"] as String).toDouble()
        scoreCancelPenalty = (properties["score.cancel"] as String).toDouble()
        scoreRefusePenalty = (properties["score.refuse"] as String).toDouble()
        scoreFinishPatronBonusMax = (properties["score.finish.patron"] as String).toDouble()
        scoreFinishReceiverPenalty = (properties["score.finish.receiver"] as String).toDouble()
        scoreNewWishPenalty = (properties["score.make_a_wish"] as String).toDouble()
    }

    fun newUser(user: User) {
        usersCollection.insertOne(user)
    }

    internal fun removeUser(user: User) {
        usersCollection.deleteOne(Filters.eq("_id", user.id))
    }

    fun getUser(telegramId: Long): User? {
        // todo optimize by telling db to stop searching after first match
        // todo index database by telegramId
        return usersCollection.find(Filters.eq("telegramId", telegramId)).first()
    }

    fun getWishUserFulfilling(telegramId: Long): Wish?{
        val user = getUser(telegramId)!!
        // todo optimize by telling db to stop searching after first match
        return wishesCollection.find(Filters.eq("patronId", user.id)).first()
    }

    fun getUserStatistics(telegramId: Long): UserStatistics {
        return UserStatistics()
    }

    fun getUserWishes(telegramId: Long): List<Wish> {
        val user = getUser(telegramId)!!
        return wishesCollection.find(Filters.eq("authorId", user.id)).toList()
    }

    fun reportUser(user: User){
        updateUserScore(user.id, scoreReportPenalty)
    }

    fun putWish(wish: Wish) {
        wishesCollection.insertOne(wish)
        updateUserScore(wish.authorId!!, scoreNewWishPenalty)
    }

    internal fun removeWish(wish: Wish) {
        wishesCollection.deleteOne(Filters.eq("_id", wish.id))
    }

    fun search(searchRequest: SearchRequest): Iterable<Wish> {
        val pipeline = mutableListOf<Bson>()
        searchRequest.searchArea?.let {
            pipeline.add(Aggregates.match(Filters.geoIntersects("wishArea", it)))
        }
        searchRequest.tagIds?.let {
            pipeline.add(Aggregates.match(Filters.`in`("tag_ids", it)))
        }
        // pipeline.add(Aggregates.sort(Sorts.descending("User.score")))  // todo: implement score

        return wishesCollection.aggregate(pipeline)
    }

    fun startWish(wish: Wish, patron: User) {
        wishesCollection.findOneAndUpdate(
            Filters.eq("_id", wish.id), Updates.set("patron_id", patron.id)
        )
    }

    fun cancelWish(patronTelegramId: Long) {
        val patron = getUser(patronTelegramId)!!
        val wish = wishesCollection.find(Filters.eq("patron_id", patron.id)).first()
        wish?.let{
            wishesCollection.findOneAndUpdate(
                Filters.eq("_id", wish.id), Updates.unset("patron_id")
            )
        }
        updateUserScore(patron.id, scoreCancelPenalty)
    }

    fun finishWish(patronTelegramId: Long) {
        val patron = getUser(patronTelegramId)!!
        val wish = wishesCollection.find(Filters.eq("patron_id", patron.id)).first()
        wish?.let{
            wishesCollection.findOneAndUpdate(
                Filters.eq("_id", wish.id), Updates.unset("patron_id")
            )
        }
        updateUserScore(patron.id, scoreFinishPatronBonusMax)
    }

    private fun updateUserScore(userId: ObjectId, scoreUpdate: Double) {
        wishesCollection.findOneAndUpdate(
            Filters.eq("_id", userId), Updates.inc("score", scoreUpdate))
    }

    fun getTag(name: String): Tag {
        return tagsCollection.find(Filters.eq("name", name)).first() ?: Tag(name).also {
            tagsCollection.insertOne(it)
        }
    }

    internal fun removeTag(tag: Tag) {
        tagsCollection.deleteOne(Filters.eq("_id", tag.id))
    }
}