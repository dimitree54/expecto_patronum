package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.RoomsDatabase
import we.rashchenko.patronum.hotel.WishRoom

class MongoRoomsDatabase(
    private val roomsCollection: MongoCollection<WishRoom>
) : RoomsDatabase {
    private val cache = mutableMapOf<Long, WishRoom>()

    override fun getByTelegramId(telegramId: Long): WishRoom? {
        return cache[telegramId] ?: roomsCollection.find(Filters.eq("telegramId", telegramId)).first()?.also {
            cache[telegramId] = it
        }
    }

    override fun new(room: WishRoom) {
        roomsCollection.insertOne(room)
        cache[room.telegramChatId] = room
    }

    override fun update(room: WishRoom) {
        roomsCollection.replaceOne(
            Filters.eq("_id", room.id), room
        )
        cache[room.telegramChatId] = room
    }

    override fun getOpen(): Iterable<WishRoom> {
        return roomsCollection.find(Filters.eq("closed", false))
    }

    override fun generateNewReportId(): String {
        return ObjectId().toHexString()
    }
}