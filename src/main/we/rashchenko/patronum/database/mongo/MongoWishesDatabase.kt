package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.WishesDatabase
import we.rashchenko.patronum.wishes.Wish
import java.time.Instant
import java.time.Period
import java.util.*

class MongoWishesDatabase(
    private val wishesCollection: MongoCollection<Wish>
) : WishesDatabase {

    private val cache = mutableMapOf<String, Wish>()
    private val closeWishAfterDays = Properties().let {
        it.load(ClassLoader.getSystemResourceAsStream("limits.properties"))
        Period.ofDays(it.getProperty("wishRemoveDelayDays").toInt())
    }

    override fun new(wish: Wish) {
        cache[wish.id] = wish
        wishesCollection.insertOne(wish)
    }

    override fun update(wish: Wish) {
        cache[wish.id] = wish
        wishesCollection.replaceOne(Filters.eq("_id", ObjectId(wish.id)), wish)
    }

    override fun get(id: String): Wish? {
        return cache[id] ?: wishesCollection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.also {
            cache[id] = it
        }
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
        wish.closed = true
        wish.expirationDate = Instant.now() + closeWishAfterDays
        update(wish)
        cache.remove(wish.id)
        wishesCollection.deleteOne(Filters.eq("_id", ObjectId(wish.id)))
    }

    override fun generateNewWishId(): String {
        return ObjectId().toHexString()
    }
}