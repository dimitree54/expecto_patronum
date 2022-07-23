package we.rashchenko.patronum.database.mongo.codecs

import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.UsersDatabase
import we.rashchenko.patronum.search.SearchInfo
import we.rashchenko.patronum.search.geo.Location
import we.rashchenko.patronum.search.geo.Polygon
import we.rashchenko.patronum.wishes.Wish
import we.rashchenko.patronum.wishes.strings.Description
import we.rashchenko.patronum.wishes.strings.Title
import java.util.*

class WishCodec(private val usersDatabase: UsersDatabase) : CollectibleCodec<Wish> {
    private val documentCodec: Codec<Document>

    init {
        documentCodec = DocumentCodec(getDefaultCodecRegistry())
    }

    override fun encode(
        bsonWriter: BsonWriter, wish: Wish, encoderContext: EncoderContext
    ) {
        val doc = Document()
        doc["_id"] = ObjectId(wish.id)
        doc["authorId"] = ObjectId(wish.author.id)
        doc["title"] = wish.title
        doc["description"] = wish.description
        doc["bounty"] = wish.bounty
        doc["creationDate"] = Date.from(wish.creationDate)
        doc["expirationDate"] = Date.from(wish.expirationDate)

        wish.searchInfo.searchArea?.let {polygon ->
            doc["search.polygon"] = polygon
            doc["search.polygon.points.latitude"] = polygon.points.map {
                it.latitude
            }
            doc["search.polygon.points.longitude"] = polygon.points.map {
                it.longitude
            }
        }
        wish.patron?.let { doc["patronId"] = ObjectId(it.id) }

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): Wish {
        val doc = documentCodec.decode(bsonReader, decoderContext)!!
        return Wish(
            id = doc.getObjectId("_id").toHexString(),
            author = usersDatabase.get(doc.getObjectId("authorId").toHexString())!!,
            title = Title(doc.getString("title")),
            description = Description(doc.getString("description")),
            bounty = doc.getDouble("bounty").toFloat(),
            creationDate = doc.getDate("creationDate").toInstant(),
            expirationDate = doc.getDate("expirationDate").toInstant(),
            searchInfo = parseSearchInfo(doc),
            patron = doc.getObjectId("patronId")?.let { usersDatabase.get(it.toHexString()) },
        )
    }

    private fun parseSearchInfo(doc: Document): SearchInfo {
        val latitudes = doc.getList("search.polygon.points.latitude", Double::class.java)
        val longitudes = doc.getList("search.polygon.points.longitude", Double::class.java)
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