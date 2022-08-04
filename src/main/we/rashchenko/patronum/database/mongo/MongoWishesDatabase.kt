package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.WishesDatabase
import we.rashchenko.patronum.wishes.Wish

class MongoWishesDatabase(
    private val wishesCollection: MongoCollection<Wish>
) : WishesDatabase {

    override fun new(wish: Wish) {
        wishesCollection.insertOne(wish)
    }

    override fun update(wish: Wish) {
        wishesCollection.replaceOne(Filters.eq("_id", ObjectId(wish.id)), wish)
    }

    override fun get(id: String): Wish? {
        return wishesCollection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    override fun getByAuthor(author: PatronUser): Iterable<Wish> {
        return wishesCollection.find(
            Filters.and(
                Filters.eq("authorId", ObjectId(author.id)),
                Filters.eq("closed", false),
            )
        )
    }

    override fun getByPatron(patron: PatronUser): Iterable<Wish> {
        return wishesCollection.find(
            Filters.and(
                Filters.eq("patronId", ObjectId(patron.id)),
                Filters.eq("closed", false),
            )
        )
    }

    override fun cancel(wish: Wish) {
        wishesCollection.deleteOne(Filters.eq("_id", ObjectId(wish.id)))
    }

    override fun generateNewWishId(): String {
        return ObjectId().toHexString()
    }
}