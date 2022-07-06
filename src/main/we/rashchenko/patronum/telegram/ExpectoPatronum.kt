package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.Update
import we.rashchenko.patronum.User
import we.rashchenko.patronum.database.Database

class ExpectoPatronum{
    private val database = Database()
    private val chatStates = mutableMapOf<Long, MainState>()

    private fun isUserInDatabase(telegramUserId: Long): Boolean {
        return database.getUser(telegramUserId) != null
    }

    private fun getUserIdFromUpdate(update: Update): Long? {
        return update.message?.from?.id ?: update.callbackQuery?.from?.id
    }

    private fun isRegistrationRequired(update: Update): Boolean {
        return getUserIdFromUpdate(update)?.let { telegramUserId ->
            val userState = chatStates.getOrPut(telegramUserId) {
                if (isUserInDatabase(telegramUserId)) MainState.MENU else MainState.NEW_USER
            }
            userState == MainState.NEW_USER
        } ?: false
    }

    private fun isMenuRequired(update: Update): Boolean {
        return getUserIdFromUpdate(update)?.let { telegramUserId ->
            val userState = chatStates[telegramUserId]
            userState == MainState.MENU
        } ?: false
    }

    fun build() = bot {
        token = System.getenv("TELEGRAM_TOKEN")
        timeout = 30

        dispatch {
            addHandler(
                RegistrationHandler(externalCheckUpdate = ::isRegistrationRequired,
                    onSuccessfulRegistration = { telegramUserId ->
                        database.newUser(User(telegramUserId))
                        chatStates[telegramUserId] = MainState.MENU
                    })
            )
            addHandler(
                MenuHandler(
                    externalCheckUpdate = ::isMenuRequired,
                    isUserFulfilling = database::isUserFulfilling,
                    isUserHaveWishes = database::isUserHaveWishes,
                    onMakeWishPressed = { chatStates[it] = MainState.MAKE_A_WISH },
                    onDoGoodPressed = { chatStates[it] = MainState.DO_GOOD },
                    onCancelFulfillmentPressed = database::cancelWish,  // todo show menu again
                    onMyWishesPressed = { chatStates[it] = MainState.MY_WISHES },
                )
            )
        }
    }
}