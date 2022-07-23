package we.rashchenko.patronum.ui.telegram.hotel.moderator

enum class ModeratorCommand(val command: String) {
    START("start"),
    FINISH("finish"),
    CANCEL("cancel"),
    REPORT("report"),
}