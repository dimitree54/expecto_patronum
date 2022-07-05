package we.rashchenko.patronum.database

import we.rashchenko.patronum.User
import we.rashchenko.patronum.Wish
import we.rashchenko.patronum.SearchRequest
import we.rashchenko.patronum.Tag

interface Database {
    fun putUser(user: User)
    fun getTag(name: String): Tag
    fun putWish(wish: Wish)
    fun search(searchRequest: SearchRequest): Iterable<Wish>
    fun startWish(wish: Wish, patron: User)
    fun finishWish(wish: Wish, patron: User)
    fun cancelWish(wish: Wish, patron: User)
}