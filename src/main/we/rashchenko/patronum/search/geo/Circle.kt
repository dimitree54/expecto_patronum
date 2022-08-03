package we.rashchenko.patronum.search.geo

import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class Circle(private val center: Location, private val radiusInMeters: Float) {
    private val earthRadiusInMeters = 6371000f
    private fun addXYToLocation(location: Location, dx: Float, dy: Float): Location {
        val latitude  = location.latitude  + (dy / earthRadiusInMeters) * (180f / Math.PI.toFloat())
        val longitude = location.longitude + (dx / earthRadiusInMeters) * (180f / Math.PI.toFloat()) /
                cos(location.latitude * Math.PI.toFloat()/180f)
        return Location(longitude, latitude)
    }
    private fun jiggleCenter(jiggleRate: Float): Location {
        val random = Random()
        val dx = jiggleRate * radiusInMeters * random.nextFloat()
        val dy = jiggleRate * radiusInMeters * random.nextFloat()
        return addXYToLocation(center, dx, dy)
    }
    fun toGeoPolygon(pointsPerCircle: Int = 16, jiggleRate: Float = 0f): Polygon{
        if (pointsPerCircle < 3){
            throw IllegalArgumentException()
        }
        val jiggledCenter = jiggleCenter(jiggleRate)
        val jiggleRadiusMeters = jiggleRate * radiusInMeters
        val random = Random()
        return Polygon(
            List(pointsPerCircle + 1) { i ->
                val jiggledRadius = jiggleRadiusMeters * random.nextFloat()
                val angle = ((i % pointsPerCircle) * 2 * Math.PI / pointsPerCircle).toFloat()
                val dx = jiggledRadius * cos(angle)
                val dy = jiggledRadius * sin(angle)
                addXYToLocation(jiggledCenter, dx, dy)
            }
        )
    }
}
