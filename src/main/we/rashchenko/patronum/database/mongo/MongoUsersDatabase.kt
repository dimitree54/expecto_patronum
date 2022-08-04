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

    override fun new(user: PatronUser) {
        usersCollection.insertOne(user)
    }

    override fun get(id: String): PatronUser? {
        return usersCollection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    override fun getByTelegramId(telegramId: Long): PatronUser? {
        return usersCollection.find(Filters.eq("telegramId", telegramId)).firstOrNull()
    }

    override fun update(user: PatronUser) {
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