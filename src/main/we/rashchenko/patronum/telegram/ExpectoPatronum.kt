package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.location
import we.rashchenko.patronum.User
import we.rashchenko.patronum.database.Database

class ExpectoPatronum {
    private val database = Database()
    private val chatStates = mutableMapOf<Long, MainState>()

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

    private fun isDoGoodRequired(telegramUserId: Long): Boolean {
        return chatStates[telegramUserId] == MainState.DO_GOOD
    }

    private fun isMyWishesRequired(telegramUserId: Long): Boolean{
        return chatStates[telegramUserId] == MainState.MY_WISHES
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
        isUserFulfilling = database::isUserFulfilling,
        isUserHaveWishes = database::isUserHaveWishes,
        onMakeWishPressed = {
            chatStates[it] = MainState.MAKE_A_WISH
            repeater.requestRepeat()
        },
        onDoGoodPressed = {
            chatStates[it] = MainState.DO_GOOD
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
            val user = database.getUser(telegramUserId)!!
            wish.authorId = user.id
            database.putWish(wish)
            chatStates[telegramUserId] = MainState.MENU
            repeater.requestRepeat()
        },
        onCancel = {
            chatStates[it] = MainState.MENU
            repeater.requestRepeat()
        }
    )

    private fun buildDoGoodHandler(repeater: Repeater) = DoGoodHandler(
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

            val doGoodHandler = buildDoGoodHandler(repeater)
            addHandler(doGoodHandler)
            repeater.addHandler(doGoodHandler)

            repeater.addHandler(repeater)  // to support several repeat requests in a row, BEWARE OF INFINITE LOOPS
            addHandler(repeater)  // Repeater must be last handler
        }
    }
}