package we.rashchenko

class User(val id: Int, val name: String){
    val stats: UserStats = UserStats()
    val favoriteTags: MutableList<Tag> = mutableListOf()
}