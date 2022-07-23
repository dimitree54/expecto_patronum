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

    private val cache = mutableMapOf<String, Wish>()

    override fun new(wish: Wish) {
        cache[wish.id] = wish
        wishesCollection.insertOne(wish)
    }

    override fun update(wish: Wish) {
        cache[wish.id] = wish
        wishesCollection.replaceOne(Filters.eq("_id", ObjectId(wish.id)), wish)
    }

    override fun get(id: String): Wish? {
        return cache[id] ?: wishesCollection.find(Filters.eq("_id", ObjectId(id))).first()?.also {
            cache[id] = it
        }
    }

    override fun getByAuthor(author: PatronUser): Iterable<Wish> {
        return wishesCollection.find(Filters.eq("author_id", author.id))
    }

    override fun getByPatron(patron: PatronUser): Iterable<Wish> {
        return wishesCollection.find(Filters.eq("patron_id", patron.id))
    }

    override fun cancel(wish: Wish) {
        cache.remove(wish.id)
        wishesCollection.deleteOne(Filters.eq("_id", ObjectId(wish.id)))
    }

    override fun generateNewWishId(): String {
        return ObjectId().toHexString()
    }
}