package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.mongo.MongoDatabaseBuilder
import we.rashchenko.patronum.database.stats.UserStats
import we.rashchenko.patronum.hotel.WishRoom
import we.rashchenko.patronum.ui.telegram.hotel.TelegramHotel
import we.rashchenko.patronum.wishes.Wish
import java.time.Period
import java.util.*

class ExpectoPatronum {
    private val database: Database = MongoDatabaseBuilder().build()
    private val chatStates = mutableMapOf<Long, MainState>()
    private val hotel: TelegramHotel

    init {
        val moderatorBotId = System.getenv("TELEGRAM_EP_USER_ID").toLong()
        hotel = TelegramHotel(moderatorBotId)
    }

    private val newUserReputation = Properties().let {
        it.load(javaClass.getResourceAsStream("reputation.properties"))
        it.getProperty("start").toFloat()
    }

    private val expirationPeriod = Properties().let {
        it.load(javaClass.getResourceAsStream("limits.properties"))
        Period.ofDays(it.getProperty("wishExpirationDays").toInt())
    }

    private enum class MainState {
        NEW_USER, MENU, MAKE_A_WISH, SEARCH, BROWSER, MY_WISHES, MANAGE_WISH
    }

    private fun isUserInDatabase(telegramUserId: Long): Boolean {
        return database.getUserByTelegramId(telegramUserId) != null
    }

    private fun isRegistrationRequired(telegramUserId: Long): Boolean {
        val userState = chatStates.getOrPut(telegramUserId) {
            if (isUserInDatabase(telegramUserId)) MainState.MENU else MainState.NEW_USER
        }
        return userState == MainState.NEW_USER
    }

    private fun isMenuRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.MENU
    }

    private fun isMakeAWishRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.MAKE_A_WISH
    }

    private fun isSearchRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.SEARCH
    }

    private fun isBrowserRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.BROWSER
    }

    private fun isMyWishesRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.MY_WISHES
    }

    private fun isManageWishRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.MANAGE_WISH
    }

    private fun buildRegistrationHandler(repeater: Repeater) =
        RegistrationHandler(externalCheckUpdate = ::isRegistrationRequired,
            onSuccessfulRegistration = { telegramUser ->
                val newUser = PatronUser(database.generateNewUserId(), telegramUser.id, UserStats(newUserReputation))
                newUser.languageCode = telegramUser.languageCode
                database.newUser(newUser)
                chatStates[telegramUser.id] = MainState.MENU
                repeater.requestRepeat()
            })

    private fun buildMenuHandler(repeater: Repeater) =
        MenuHandler(externalCheckUpdate = ::isMenuRequired, getWishUserFulfilling = { telegramUserId ->
            database.getUserByTelegramId(telegramUserId)?.let {
                database.getWishesByPatron(it).first()
            }
        }, getUserStatistics = {
            database.getUserByTelegramId(it)!!.stats
        }, getGlobalStatistics = {
            database.getGlobalStats()
        }, onMakeWishPressed = {
            chatStates[it] = MainState.MAKE_A_WISH
            repeater.requestRepeat()
        }, onSearchPressed = {
            chatStates[it] = MainState.SEARCH
            repeater.requestRepeat()
        }, onCancelFulfillmentPressed = {
            val wish = database.getWishesByPatron(database.getUserByTelegramId(it)!!).first()
            database.cancelWishByPatron(wish)
            repeater.requestRepeat()
        }, onMyWishesPressed = {
            chatStates[it] = MainState.MY_WISHES
            repeater.requestRepeat()
        })

    private fun buildMakeAWishHandler(repeater: Repeater) =
        MakeAWishHandler(externalCheckUpdate = ::isMakeAWishRequired, onWishCreated = { telegramUserId, wishDraft ->
            database.newWish(
                wishDraft.toWish(
                    database.generateNewWishId(), database.getUserByTelegramId(telegramUserId)!!, expirationPeriod
                )
            )
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildSearchHandler(repeater: Repeater, browserHandler: BrowserHandler) = SearchHandler(
        externalCheckUpdate = ::isSearchRequired,
        onSearchRequestCreated = { telegramUserId, searchRequestDraft ->
            val patron = database.getUserByTelegramId(telegramUserId)!!
            val searchResults = database.search(patron, searchRequestDraft.toSearchInfo())
            browserHandler.registerSearchResults(telegramUserId, searchResults)
            chatStates[telegramUserId] = MainState.BROWSER
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun acceptWish(patronTelegramId: Long, wish: Wish) {
        val patron = database.getUserByTelegramId(patronTelegramId)!!
        database.acceptWish(patron, wish)
        val roomTelegramId = hotel.openRoom(wish.title.value, listOf(wish.author.telegramId, patronTelegramId))
        val newRoom = WishRoom(database.generateNewRoomId(), roomTelegramId, wish).apply {
            wish.author.languageCode?.let { addLanguageCode(it) }
            patron.languageCode?.let { addLanguageCode(it) }
        }
        database.openWishRoom(newRoom)
    }

    private fun buildBrowserHandler(repeater: Repeater) =
        BrowserHandler(externalCheckUpdate = ::isBrowserRequired, onMatch = { telegramUserId, wish ->
            acceptWish(telegramUserId, wish)
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        }, onSkip = { telegramUserId, wish ->
            val patron = database.getUserByTelegramId(telegramUserId)!!
            database.skipWish(patron, wish)
        }, onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildManageWishHandler(repeater: Repeater) =
        ManageWishHandler(externalCheckUpdate = ::isManageWishRequired, onWishDelete = { telegramUserId, wish ->
            database.cancelWishByAuthor(wish)
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildMyWishesHandler(repeater: Repeater, manageWishHandler: ManageWishHandler) =
        MyWishesHandler(externalCheckUpdate = ::isMyWishesRequired, getUserWishes = { telegramUserId ->
            val author = database.getUserByTelegramId(telegramUserId)!!
            database.getWishesByAuthor(author).toList()
        }, onWishChosen = { telegramUserId, wish ->
            chatStates[telegramUserId] = MainState.MANAGE_WISH
            manageWishHandler.registerChosenWish(telegramUserId, wish)
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        })

    fun build() = bot {
        token = System.getenv("TELEGRAM_EP_MAIN_BOT_TOKEN")
        timeout = 30
        val repeater = Repeater()

        dispatch {
            val registrationHandler = buildRegistrationHandler(repeater)
            addHandler(registrationHandler)
            repeater.addHandler(registrationHandler)

            val menuHandler = buildMenuHandler(repeater)
            addHandler(menuHandler)
            repeater.addHandler(menuHandler)

            val makeAWishHandler = buildMakeAWishHandler(repeater)
            addHandler(makeAWishHandler)
            repeater.addHandler(makeAWishHandler)

            val browserHandler = buildBrowserHandler(repeater)
            val searchHandler = buildSearchHandler(repeater, browserHandler)
            addHandler(searchHandler)
            repeater.addHandler(searchHandler)
            addHandler(browserHandler)
            repeater.addHandler(browserHandler)

            val manageWishHandler = buildManageWishHandler(repeater)
            val myWishesHandler = buildMyWishesHandler(repeater, manageWishHandler)
            addHandler(myWishesHandler)
            repeater.addHandler(myWishesHandler)
            addHandler(manageWishHandler)
            repeater.addHandler(manageWishHandler)

            repeater.addHandler(repeater)  // to support several repeat requests in a row, BEWARE OF INFINITE LOOPS
            addHandler(repeater)  // Repeater must be last handler
        }
    }
}