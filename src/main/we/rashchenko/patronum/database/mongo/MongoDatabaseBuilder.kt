package we.rashchenko.patronum.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromCodecs
import we.rashchenko.patronum.database.*
import we.rashchenko.patronum.database.mongo.codecs.PatronUserCodec
import we.rashchenko.patronum.database.mongo.codecs.ReportCodec
import we.rashchenko.patronum.database.mongo.codecs.RoomCodec
import we.rashchenko.patronum.database.mongo.codecs.WishCodec
import we.rashchenko.patronum.database.stats.Report
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.wishes.Wish
import java.util.*

class MongoDatabaseBuilder {
    private val usersDatabase: UsersDatabase
    private val wishesDatabase: WishesDatabase
    private val reportsDatabase: ReportsDatabase
    private val roomsDatabase: RoomsDatabase
    private val searchEngine: SearchEngine

    private fun getMongoClient(connectionString: String): MongoClient {
        val settings: MongoClientSettings = MongoClientSettings.builder().applyConnectionString(ConnectionString(connectionString))
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build()
        return MongoClients.create(settings)
    }

    init {
        val database = Properties().let {
            it.load(ClassLoader.getSystemResourceAsStream("mongo.properties"))
            getMongoClient(
                System.getenv("MONGODB_CONNECTION")
            ).getDatabase(it["database"] as String)
        }

        val usersCollection = database.getCollection("users", PatronUser::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(PatronUserCodec())
            )
        )
        usersDatabase = MongoUsersDatabase(usersCollection)
        val wishesCollection = database.getCollection("wishes", Wish::class.java).withCodecRegistry(
            CodecRegistries.fromRegistries(
                getDefaultCodecRegistry(), fromCodecs(WishCodec())
            )
        )
        wishesDatabase = MongoWishesDatabase(wishesCollection)
        searchEngine = MongoSearchEngine(wishesCollection, usersCollection)

        reportsDatabase = MongoReportsDatabase(
            database.getCollection("reports", Report::class.java).withCodecRegistry(
                CodecRegistries.fromRegistries(
                    getDefaultCodecRegistry(), fromCodecs(ReportCodec(usersDatabase))
                )
            )
        )
        roomsDatabase = MongoRoomsDatabase(
            database.getCollection("rooms", WishRoom::class.java).withCodecRegistry(
                CodecRegistries.fromRegistries(
                    getDefaultCodecRegistry(), fromCodecs(RoomCodec())
                )
            )
        )
    }

    fun build() = Database(
        users = usersDatabase,
        wishes = wishesDatabase,
        reports = reportsDatabase,
        searchEngine = searchEngine,
        rooms = roomsDatabase,
    )
}