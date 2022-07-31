package we.rashchenko.patronum.search.geo

import kotlin.math.cos
import kotlin.math.sin

class Circle(private val center: Location, private val radiusInMeters: Float) {
    private val earthRadiusInMeters = 6371000f
    fun toGeoPolygon(pointsPerCircle: Int = 16): Polygon{
        if (pointsPerCircle < 3){
            throw IllegalArgumentException()
        }
        return Polygon(
            List(pointsPerCircle + 1) { i ->
                val angle = ((i % pointsPerCircle) * 2 * Math.PI / pointsPerCircle).toFloat()
                val dx = radiusInMeters * cos(angle)
                val dy = radiusInMeters * sin(angle)
                val latitude  = center.latitude  + (dy / earthRadiusInMeters) * (180f / Math.PI.toFloat())
                val longitude = center.longitude + (dx / earthRadiusInMeters) * (180f / Math.PI.toFloat()) /
                        cos(center.latitude * Math.PI.toFloat()/180f)
                Location(longitude, latitude)
            }
        )
    }
}
