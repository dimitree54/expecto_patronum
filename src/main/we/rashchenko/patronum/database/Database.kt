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
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromCodecs
import org.bson.conversions.Bson
import we.rashchenko.patronum.SearchRequest
import we.rashchenko.patronum.Tag
import we.rashchenko.patronum.User
import we.rashchenko.patronum.Wish

class Database {
    private val mongoClient: MongoClient
    private val database: MongoDatabase
    private val usersCollection: MongoCollection<User>
    private val wishesCollection: MongoCollection<Wish>
    private val tagsCollection: MongoCollection<Tag>

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
        ).apply {
            createIndex(Indexes.geo2dsphere("wishArea"))
        }
        tagsCollection = database.getCollection("tags", Tag::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(TagCodec())
            )
        )
    }

    fun putUser(user: User) {
        usersCollection.insertOne(user)
    }

    fun removeUser(user: User) {
        usersCollection.deleteOne(Filters.eq("_id", user.id))
    }

    fun putWish(wish: Wish) {
        wishesCollection.insertOne(wish)
    }

    fun removeWish(wish: Wish) {
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

    fun cancelWish(wish: Wish, patron: User) {
        wishesCollection.findOneAndUpdate(
            Filters.eq("_id", wish.id), Updates.unset("patron_id")
        )
    }

    fun finishWish(wish: Wish, patron: User) {
        removeWish(wish)
        // todo: increase patron's score
    }

    fun getTag(name: String): Tag {
        return tagsCollection.find(Filters.eq("name", name)).first() ?: Tag(name).also {
            tagsCollection.insertOne(it)
        }
    }

    fun removeTag(tag: Tag) {
        tagsCollection.deleteOne(Filters.eq("_id", tag.id))
    }
}