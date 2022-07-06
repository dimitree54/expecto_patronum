package we.rashchenko.database.mongo

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import we.rashchenko.patronum.SearchRequest
import we.rashchenko.patronum.User
import we.rashchenko.patronum.Wish
import we.rashchenko.patronum.circle2polygon
import we.rashchenko.patronum.database.Database
import java.util.*

internal class DatabaseTest {
    private val database = Database()
    private val user = User(-1L)
    private val tag1 = database.getTag("testTag1")
    private val tag2 = database.getTag("testTag2")
    private val wish = Wish(user.id).also {
        it.title = "testWish1"
        it.description = "testDescription1"
        it.tagIds.add(tag1.id)
        it.wishArea = circle2polygon(0.0, 0.0, 10.0)
        it.expirationDate = Calendar.Builder().setDate(2100, 1, 1).build().time
    }


    @BeforeEach
    fun init() {
        database.newUser(user)
        database.putWish(wish)
    }

    @Test
    fun searchByTag() {
        val potentialPatron = User(-2L)
        val res1= database.search(SearchRequest(potentialPatron).also { it.tagIds = listOf(tag1.id) }).toList()
        assert(res1.size == 1)
        assert(res1[0].id == wish.id)
        val res2 = database.search(SearchRequest(potentialPatron).also { it.tagIds = listOf(tag2.id) }).toList()
        assert(res2.isEmpty())
    }

    @Test
    fun searchByLocation() {
        val potentialPatron = User(-1L)
        val res1 = database.search(SearchRequest(potentialPatron).apply { searchArea = circle2polygon(20.0, 0.0, 11.0) }).toList()
        assert(res1.size == 1)
        assert(res1[0].id == wish.id)
        val res2 = database.search(SearchRequest(potentialPatron).apply { searchArea = circle2polygon(20.0, 0.0, 9.0) }).toList()
        assert(res2.isEmpty())
    }

    @Test
    fun searchPriority() {
    }

    @Test
    fun startWish() {
    }

    @Test
    fun cancelWish() {
    }

    @Test
    fun finishWish() {

    }

    @AfterEach
    fun clean() {
        database.removeUser(user)
        database.removeTag(tag1)
        database.removeTag(tag2)
        database.removeWish(wish)
    }
}