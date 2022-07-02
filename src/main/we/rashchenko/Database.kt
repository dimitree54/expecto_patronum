package we.rashchenko

abstract class Database {
    fun getUser(id: Int): User? {
        return null
    }
    fun getWish(id: Int): Wish? {
        return null
    }
    fun getTag(id: Int): Tag? {
        return null
    }
    fun putUser(user: User) {
    }
    fun putWish(wish: Wish) {
    }
    fun putTag(tag: Tag) {
    }
    abstract fun search(searchRequest: SearchRequest): List<Wish>
}