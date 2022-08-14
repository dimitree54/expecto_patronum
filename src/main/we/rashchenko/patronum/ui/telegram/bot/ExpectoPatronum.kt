package we.rashchenko.patronum.ui.telegram.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.logging.LogLevel
import we.rashchenko.patronum.database.Database
import we.rashchenko.patronum.database.mongo.MongoDatabaseBuilder
import we.rashchenko.patronum.errors.AlreadyFulfillingError
import we.rashchenko.patronum.errors.NotFulfillingError
import we.rashchenko.patronum.errors.UserNotExistError
import we.rashchenko.patronum.ui.telegram.hotel.TelegramHotel
import we.rashchenko.patronum.wishes.Wish

class ExpectoPatronum {
    private val database: Database = MongoDatabaseBuilder().build()
    private val chatStates = mutableMapOf<Long, MainState>()
    private val hotel: TelegramHotel

    init {
        val moderatorBotId = System.getenv("TELEGRAM_EP_USER_ID").toLong()
        hotel = TelegramHotel(moderatorBotId)
    }

    private enum class MainState {
        MENU, MAKE_A_WISH, SEARCH, BROWSER, MY_WISHES, MANAGE_WISH
    }

    private fun isRegistrationRequired(telegramUser: User): Boolean {
        return try {
            database.getUserByTelegramId(telegramUser.id)
            if (chatStates[telegramUser.id] == null) {
                chatStates[telegramUser.id] = MainState.MENU
            }
            false
        } catch (e: UserNotExistError) {
            true
        }
    }

    private fun isMenuRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.MENU
    }

    private fun isMakeAWishRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.MAKE_A_WISH
    }

    private fun isSearchRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.SEARCH
    }

    private fun isBrowserRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.BROWSER
    }

    private fun isMyWishesRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.MY_WISHES
    }

    private fun isManageWishRequired(telegramUser: User): Boolean {
        return chatStates[telegramUser.id] == MainState.MANAGE_WISH
    }

    private fun buildRegistrationHandler(repeater: Repeater) =
        RegistrationHandler(externalCheckUpdate = ::isRegistrationRequired, onSuccessfulRegistration = { telegramUser ->
            database.newUser(telegramUser.id, telegramUser.languageCode)
            chatStates[telegramUser.id] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildMenuHandler(repeater: Repeater) =
        MenuHandler(externalCheckUpdate = ::isMenuRequired, getWishUserFulfilling = { telegramUser ->
            val patron = database.getUserByTelegramId(telegramUser.id)
            database.getWishesByPatron(patron).firstOrNull()
        }, getUserStatistics = {
            database.getUserByTelegramId(it.id).stats
        }, getGlobalStatistics = {
            database.getGlobalStats()
        }, onMakeWishPressed = {
            chatStates[it.id] = MainState.MAKE_A_WISH
            repeater.requestRepeat()
        }, onSearchPressed = {
            val patron = database.getUserByTelegramId(it.id)
            val fulfillingWishes = database.getWishesByPatron(patron).toList()
            if (fulfillingWishes.isNotEmpty()) throw AlreadyFulfillingError()
            chatStates[it.id] = MainState.SEARCH
            repeater.requestRepeat()
        }, onCancelFulfillmentPressed = {
            val wish = database.getWishesByPatron(database.getUserByTelegramId(it.id)).firstOrNull()
                ?: throw NotFulfillingError()
            database.cancelFulfillmentByPatron(wish)
            repeater.requestRepeat()
        }, onMyWishesPressed = {
            chatStates[it.id] = MainState.MY_WISHES
            repeater.requestRepeat()
        }, onRefreshPressed = {
            repeater.requestRepeat()
        })

    private fun buildMakeAWishHandler(repeater: Repeater) =
        MakeAWishHandler(externalCheckUpdate = ::isMakeAWishRequired, onWishCreated = { telegramUser, wishDraft ->
            database.newWish(telegramUser.id, wishDraft)
            chatStates[telegramUser.id] = MainState.MENU
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it.id] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildSearchHandler(repeater: Repeater, browserHandler: BrowserHandler) = SearchHandler(
        externalCheckUpdate = ::isSearchRequired,
        onSearchRequestCreated = { telegramUser, searchRequestDraft ->
            val patron = database.getUserByTelegramId(telegramUser.id)
            val searchResults = database.search(patron, searchRequestDraft.toSearchInfo())
            browserHandler.registerSearchResults(telegramUser.id, searchResults)
            chatStates[telegramUser.id] = MainState.BROWSER
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it.id] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun acceptWish(patronTelegram: User, wish: Wish) {
        val author = database.getUserById(wish.authorId)
        val patron = database.getUserByTelegramId(patronTelegram.id)
        database.acceptWish(patron, wish)
        hotel.openRoom(wish.title.text, listOf(author.telegramId, patronTelegram.id)) { roomTelegramId ->
            database.openWishRoom(roomTelegramId, wish)
        }
    }

    private fun buildBrowserHandler(repeater: Repeater) =
        BrowserHandler(externalCheckUpdate = ::isBrowserRequired, onMatch = { telegramUser, wish ->
            acceptWish(telegramUser, wish)
            chatStates[telegramUser.id] = MainState.MENU
            repeater.requestRepeat()
        }, onSkip = { telegramUser, wish ->
            val patron = database.getUserByTelegramId(telegramUser.id)
            database.skipWish(patron, wish)
        }, onCancel = {
            chatStates[it.id] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildManageWishHandler(repeater: Repeater) =
        ManageWishHandler(externalCheckUpdate = ::isManageWishRequired, onWishDelete = { telegramUser, wish ->
            database.cancelWishByAuthor(wish)
            chatStates[telegramUser.id] = MainState.MENU
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it.id] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildMyWishesHandler(repeater: Repeater, manageWishHandler: ManageWishHandler) =
        MyWishesHandler(externalCheckUpdate = ::isMyWishesRequired, getUserWishes = { telegramUser ->
            val author = database.getUserByTelegramId(telegramUser.id)
            database.getWishesByAuthor(author).toList()
        }, onWishChosen = { telegramUser, wish ->
            chatStates[telegramUser.id] = MainState.MANAGE_WISH
            manageWishHandler.registerChosenWish(telegramUser.id, wish)
            repeater.requestRepeat()
        }, onCancel = {
            chatStates[it.id] = MainState.MENU
            repeater.requestRepeat()
        })

    fun build() = bot {
        token = System.getenv("TELEGRAM_EP_MAIN_BOT_TOKEN")
        timeout = 30
        logLevel = LogLevel.Error
        val repeater = Repeater()

        dispatch {
            val registrationHandler = buildRegistrationHandler(repeater)
            val safeRegistrationHandler = SafeHandlerWrapper(registrationHandler, repeater)
            addHandler(safeRegistrationHandler)
            repeater.addHandler(safeRegistrationHandler)

            val menuHandler = buildMenuHandler(repeater)
            val safeMenuHandler = SafeHandlerWrapper(menuHandler, repeater)
            addHandler(safeMenuHandler)
            repeater.addHandler(safeMenuHandler)

            val makeAWishHandler = buildMakeAWishHandler(repeater)
            val safeMakeAWishHandler = SafeHandlerWrapper(makeAWishHandler, repeater)
            addHandler(safeMakeAWishHandler)
            repeater.addHandler(safeMakeAWishHandler)

            val browserHandler = buildBrowserHandler(repeater)
            val searchHandler = buildSearchHandler(repeater, browserHandler)
            val safeBrowserHandler = SafeHandlerWrapper(browserHandler, repeater)
            val safeSearchHandler = SafeHandlerWrapper(searchHandler, repeater)
            addHandler(safeSearchHandler)
            repeater.addHandler(safeSearchHandler)
            addHandler(safeBrowserHandler)
            repeater.addHandler(safeBrowserHandler)

            val manageWishHandler = buildManageWishHandler(repeater)
            val myWishesHandler = buildMyWishesHandler(repeater, manageWishHandler)
            val safeManageWishHandler = SafeHandlerWrapper(manageWishHandler, repeater)
            val safeMyWishesHandler = SafeHandlerWrapper(myWishesHandler, repeater)
            addHandler(safeMyWishesHandler)
            repeater.addHandler(safeMyWishesHandler)
            addHandler(safeManageWishHandler)
            repeater.addHandler(safeManageWishHandler)

            repeater.addHandler(repeater)  // to support several repeat requests in a row, BEWARE OF INFINITE LOOPS
            addHandler(repeater)  // Repeater have to be last handler
        }
    }
}