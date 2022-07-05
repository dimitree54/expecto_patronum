package we.rashchenko.database

import we.rashchenko.SearchRequest
import we.rashchenko.Tag
import we.rashchenko.User
import we.rashchenko.Wish

interface Database {
    fun putUser(user: User)
    fun getTag(name: String): Tag
    fun putWish(wish: Wish)
    fun search(searchRequest: SearchRequest): Iterable<Wish>
    fun startWish(wish: Wish, patron: User)
    fun finishWish(wish: Wish, patron: User)
    fun cancelWish(wish: Wish, patron: User)
}