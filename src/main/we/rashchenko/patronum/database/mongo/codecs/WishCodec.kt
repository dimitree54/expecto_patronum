package we.rashchenko.patronum.database.mongo.codecs

import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.mongo.toMongo
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.search.geo.Location
import we.rashchenko.patronum.search.geo.Polygon
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title
import java.util.*

class WishCodec : CollectibleCodec<Wish> {
    private val documentCodec: Codec<Document>

    init {
        documentCodec = DocumentCodec(getDefaultCodecRegistry())
    }

    override fun encode(
        bsonWriter: BsonWriter, wish: Wish, encoderContext: EncoderContext
    ) {
        val doc = Document()
        doc["_id"] = ObjectId(wish.id)
        doc["authorId"] = ObjectId(wish.authorId)
        doc["title"] = wish.title.text
        doc["description"] = wish.description.text
        doc["bounty"] = wish.bounty
        doc["creationDate"] = Date.from(wish.creationDate)
        doc["expirationDate"] = Date.from(wish.expirationDate)
        doc["closed"] = wish.closed

        wish.searchInfo.searchArea?.let {polygon ->
            doc["searchPolygon"] = polygon.toMongo()
            doc["searchPolygonPointsLatitude"] = polygon.points.map {
                it.latitude
            }
            doc["searchPolygonPointsLongitude"] = polygon.points.map {
                it.longitude
            }
        }
        wish.patronId?.let { doc["patronId"] = ObjectId(it) }

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): Wish {
        val doc = documentCodec.decode(bsonReader, decoderContext)!!
        return Wish(
            id = doc.getObjectId("_id").toHexString(),
            authorId = doc.getObjectId("authorId").toHexString(),
            title = Title(doc.getString("title")),
            description = Description(doc.getString("description")),
            bounty = doc.getInteger("bounty"),
            creationDate = doc.getDate("creationDate").toInstant(),
            expirationDate = doc.getDate("expirationDate").toInstant(),
            searchInfo = parseSearchInfo(doc),
            patronId = doc.getObjectId("patronId")?.toHexString(),
            closed = doc.getBoolean("closed")
        )
    }

    private fun parseSearchInfo(doc: Document): SearchInfo {
        val latitudes = doc.getList("searchPolygonPointsLatitude", Double::class.javaObjectType) ?: return SearchInfo()
        val longitudes = doc.getList("searchPolygonPointsLongitude", Double::class.javaObjectType) ?: return SearchInfo()
        return SearchInfo(
            Polygon(
                (latitudes zip longitudes).map {
                    Location(longitude = it.second.toFloat(), latitude = it.first.toFloat())
                }
            )
        )
    }

    override fun getEncoderClass(): Class<Wish> {
        return Wish::class.java
    }

    override fun generateIdIfAbsentFromDocument(wish: Wish) = wish

    override fun documentHasId(actor: Wish) = true

    override fun getDocumentId(actor: Wish) = BsonString(actor.id)
}