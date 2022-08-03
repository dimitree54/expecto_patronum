package we.rashchenko.patronum.search

import we.rashchenko.patronum.search.geo.Circle
import we.rashchenko.patronum.search.geo.Location

class SearchInfoDraft {
    private val jiggleRate = 0.1f
    var location: Location? = null
    var radius: Float? = null
    fun toSearchInfo(): SearchInfo {
        val searchArea = location?.let { l ->
            radius?.let{r ->
                Circle(l, r).toGeoPolygon(jiggleRate = jiggleRate)
            }
        }
        return SearchInfo(searchArea)
    }
}