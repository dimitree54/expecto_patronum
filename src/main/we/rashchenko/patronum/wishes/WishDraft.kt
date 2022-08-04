package we.rashchenko.patronum.wishes

import we.rashchenko.patronum.errors.WishDraftNotFilledError
import we.rashchenko.patronum.search.SearchInfoDraft
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title
import java.time.Instant
import java.time.Period

class WishDraft{
    var title: Title? = null
    var description: Description? = null
    var searchInfoDraft: SearchInfoDraft? = null
    fun toWish(id: String, authorId: String, validityPeriod: Period): Wish{
        if (title == null || description == null || searchInfoDraft == null){
            throw WishDraftNotFilledError()
        }
        return Wish(
            id,
            authorId,
            title!!,
            description!!,
            searchInfoDraft!!.toSearchInfo(),
            Instant.now(),
            Instant.now() + validityPeriod
        )
    }
}