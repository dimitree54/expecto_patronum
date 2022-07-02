package we.rashchenko

class SearchRequest(val id: Int) {
    var tags: List<Tag>? = null
    var location: Location? = null
}