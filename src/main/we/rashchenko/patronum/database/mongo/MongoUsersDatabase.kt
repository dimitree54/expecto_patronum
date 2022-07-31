package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.UsersDatabase
import we.rashchenko.patronum.database.stats.GlobalStats

class MongoUsersDatabase(
    private val usersCollection: MongoCollection<PatronUser>
) : UsersDatabase {
    private val cache = mutableMapOf<String, PatronUser>()
    private val telegramCache = mutableMapOf<Long, PatronUser>()

    override fun new(user: PatronUser) {
        usersCollection.insertOne(user)
        cache[user.id] = user
        telegramCache[user.telegramId] = user
    }

    override fun get(id: String): PatronUser? {
        return cache[id] ?: usersCollection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.also {
            cache[id] = it
            telegramCache[it.telegramId] = it
        }
    }

    override fun getByTelegramId(telegramId: Long): PatronUser? {
        return telegramCache[telegramId] ?: usersCollection.find(Filters.eq("telegramId", telegramId)).firstOrNull()?.also {
            cache[it.id] = it
            telegramCache[it.telegramId] = it
        }
    }

    override fun update(user: PatronUser) {
        cache[user.id] = user
        telegramCache[user.telegramId] = user
        usersCollection.replaceOne(Filters.eq("_id", ObjectId(user.id)), user)
    }

    override fun getGlobalStats(): GlobalStats {
        val allUsers = usersCollection.find().toList()
        return GlobalStats().apply{
            sortedReputations = allUsers.map { it.stats.reputation }.toTypedArray()
        }
    }

    override fun generateNewUserId(): String {
        return ObjectId().toHexString()
    }
}