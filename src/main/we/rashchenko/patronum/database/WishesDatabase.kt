package we.rashchenko.patronum.database

import we.rashchenko.patronum.wishes.Wish

interface WishesDatabase{
    fun new(wish: Wish)
    fun update(wish: Wish)
    fun get(id: String): Wish?
    fun getByAuthor(author: PatronUser): Iterable<Wish>
    fun getByPatron(patron: PatronUser): Iterable<Wish>
    fun cancel(wish: Wish)
    fun generateNewWishId(): String
}