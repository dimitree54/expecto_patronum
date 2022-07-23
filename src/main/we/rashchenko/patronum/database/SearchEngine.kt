package we.rashchenko.patronum.database

import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.wishes.Wish

interface SearchEngine {
    fun search(patron: PatronUser, query: SearchInfo): Iterable<Wish>
}