package we.rashchenko.patronum.errors

abstract class UserReadableError(message: String): Error(message) {
    abstract fun getUserReadableMessage(languageCode: String?): String
}