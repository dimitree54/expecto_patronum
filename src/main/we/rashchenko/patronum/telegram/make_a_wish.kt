package we.rashchenko.patronum.telegram

import com.github.kotlintelegrambot.Bot
import we.rashchenko.patronum.database.Database


fun Bot.Builder.makeAWish(database: Database, telegramUserId: Long): MainState {
    return MainState.MENU
}
