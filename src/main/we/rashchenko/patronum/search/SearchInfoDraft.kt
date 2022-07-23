package we.rashchenko.patronum.search

import we.rashchenko.patronum.search.geo.Circle
import we.rashchenko.patronum.search.geo.Location

class SearchInfoDraft {
    var location: Location? = null
    var radius: Float? = null
    fun toSearchInfo(): SearchInfo {
        val searchArea = location?.let { l ->
            radius?.let{r ->
                Circle(l, r).toGeoPolygon()
            }
        }
        return SearchInfo(searchArea)
    }
}