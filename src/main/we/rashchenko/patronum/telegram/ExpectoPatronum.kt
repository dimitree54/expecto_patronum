package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import we.rashchenko.patronum.User
import we.rashchenko.patronum.database.Database

class ExpectoPatronum {
    private val database = Database()
    private val chatStates = mutableMapOf<Long, MainState>()

    private enum class MainState {
        NEW_USER, MENU, MAKE_A_WISH, SEARCH, BROWSER, MY_WISHES, MANAGE_WISH
    }

    private fun isUserInDatabase(telegramUserId: Long): Boolean {
        return database.getUser(telegramUserId) != null
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

    private fun isBrowserRequired(telegramUserId: Long): Boolean{
        return chatStates[telegramUserId] == MainState.BROWSER
    }

    private fun isMyWishesRequired(telegramUserId: Long): Boolean{
        return chatStates[telegramUserId] == MainState.MY_WISHES
    }

    private fun isManageWishRequired(telegramUserId: Long): Boolean{
        return chatStates[telegramUserId] == MainState.MANAGE_WISH
    }

    private fun buildRegistrationHandler(repeater: Repeater) = RegistrationHandler(
        externalCheckUpdate = ::isRegistrationRequired,
        onSuccessfulRegistration = { telegramUserId ->
            database.newUser(User(telegramUserId))
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        })

    private fun buildMenuHandler(repeater: Repeater) = MenuHandler(
        externalCheckUpdate = ::isMenuRequired,
        getWishUserFulfilling = database::getWishUserFulfilling,
        getUserStatistics = database::getUserStatistics,
        onMakeWishPressed = {
            chatStates[it] = MainState.MAKE_A_WISH
            repeater.requestRepeat()
        },
        onSearchPressed = {
            chatStates[it] = MainState.SEARCH
            repeater.requestRepeat()
        },
        onCancelFulfillmentPressed = {
            database::cancelWish
            repeater.requestRepeat()
        },
        onMyWishesPressed = {
            chatStates[it] = MainState.MY_WISHES
            repeater.requestRepeat()
        }
    )

    private fun buildMakeAWishHandler(repeater: Repeater) = MakeAWishHandler(
        externalCheckUpdate = ::isMakeAWishRequired,
        onWishCreated = { telegramUserId, wish ->
            wish.authorId = database.getUser(telegramUserId)!!.id
            database.putWish(wish)
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    private fun buildSearchHandler(repeater: Repeater, browserHandler: BrowserHandler) = SearchHandler(
        externalCheckUpdate = ::isSearchRequired,
        onSearchRequestCreated = { telegramUserId, searchRequest ->
            searchRequest.author = database.getUser(telegramUserId)!!
            browserHandler.registerSearchRequest(telegramUserId, searchRequest)
            chatStates[telegramUserId] = MainState.BROWSER
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    private fun buildBrowserHandler(repeater: Repeater) = BrowserHandler(
        database,
        externalCheckUpdate = ::isBrowserRequired,
        onMatch = { telegramUserId, wish ->
            //establishContact()
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    private fun buildManageWishHandler(repeater: Repeater) = ManageWishHandler(
        externalCheckUpdate = ::isManageWishRequired,
        onWishDelete = { telegramUserId, wish ->
            database.removeWish(wish)
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    private fun buildMyWishesHandler(repeater: Repeater, manageWishHandler: ManageWishHandler) = MyWishesHandler(
        externalCheckUpdate = ::isMyWishesRequired,
        getUserWishes = database::getUserWishes,
        onWishChosen = { telegramUserId, wish ->
            chatStates[telegramUserId] = MainState.MANAGE_WISH
            manageWishHandler.registerChosenWish(telegramUserId, wish)
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    fun build() = bot {
        token = System.getenv("TELEGRAM_TOKEN")
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