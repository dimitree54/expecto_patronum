package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.RoomsDatabase
import we.rashchenko.patronum.hotel.WishRoom

class MongoRoomsDatabase(
    private val roomsCollection: MongoCollection<WishRoom>
) : RoomsDatabase {

    override fun getByTelegramId(telegramId: Long): WishRoom? {
        return roomsCollection.find(Filters.eq("telegramId", telegramId)).firstOrNull()
    }

    override fun new(room: WishRoom) {
        roomsCollection.insertOne(room)
    }

    override fun update(room: WishRoom) {
        roomsCollection.replaceOne(
            Filters.eq("_id", ObjectId(room.id)), room
        )
    }

    override fun getOpen(): Iterable<WishRoom> {
        return roomsCollection.find(Filters.eq("closed", false))
    }

    override fun generateNewReportId(): String {
        return ObjectId().toHexString()
    }
}