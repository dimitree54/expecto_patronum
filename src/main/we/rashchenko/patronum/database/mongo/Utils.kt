package we.rashchenko.patronum.database.mongo

import we.rashchenko.patronum.search.geo.Location
import we.rashchenko.patronum.search.geo.Polygon


fun Location.toMongo(): com.mongodb.client.model.geojson.Position{
    return com.mongodb.client.model.geojson.Position(latitude.toDouble(), longitude.toDouble())
}

fun Polygon.toMongo(): com.mongodb.client.model.geojson.Polygon{
    return com.mongodb.client.model.geojson.Polygon(
        this.points.map { it.toMongo() }
    )
}