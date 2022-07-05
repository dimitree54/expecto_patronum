package we.rashchenko.patronum

import com.mongodb.client.model.geojson.Polygon
import com.mongodb.client.model.geojson.Position
import kotlin.math.cos
import kotlin.math.sin

const val POINTS_PER_CIRCLE = 16
fun circle2polygon(x: Double, y: Double, radius: Double): Polygon {
    return Polygon(
        List(POINTS_PER_CIRCLE + 1) { i ->
            val angle = ((i % POINTS_PER_CIRCLE) * 2 * Math.PI / POINTS_PER_CIRCLE)
            Position(x + radius * cos(angle), y + radius * sin(angle))
        }
    )
}