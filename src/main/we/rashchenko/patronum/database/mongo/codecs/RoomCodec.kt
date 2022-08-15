package we.rashchenko.patronum.database.mongo.codecs

import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.types.ObjectId
import we.rashchenko.patronum.hotel.WishRoom

class RoomCodec : CollectibleCodec<WishRoom> {
    private val documentCodec = DocumentCodec()

    override fun encode(
        bsonWriter: BsonWriter, room: WishRoom, encoderContext: EncoderContext
    ) {
        val doc = Document()
        doc["_id"] = ObjectId(room.id)
        doc["telegramId"] = room.telegramChatId
        doc["wishId"] = ObjectId(room.wishId)
        doc["authorId"] = ObjectId(room.authorId)
        doc["patronId"] = ObjectId(room.patronId)
        doc["languageCodes"] = room.getLanguageCodes().toList()
        doc["reportedByAuthor"] = room.reportedByAuthor
        doc["reportedByPatron"] = room.reportedByPatron

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): WishRoom {
        val doc = documentCodec.decode(bsonReader, decoderContext)
        return WishRoom(
            id = (doc.getObjectId("_id")).toHexString(),
            telegramChatId = doc.getLong("telegramId"),
            wishId = doc.getObjectId("wishId").toHexString(),
            authorId = doc.getObjectId("authorId").toHexString(),
            patronId = doc.getObjectId("patronId").toHexString()
        ).apply {
            doc.getList("languageCodes", String::class.java).forEach {
                addLanguageCode(it)
            }
            reportedByAuthor = doc.getBoolean("reportedByAuthor")
            reportedByPatron = doc.getBoolean("reportedByPatron")
        }
    }

    override fun getEncoderClass() = WishRoom::class.java

    override fun generateIdIfAbsentFromDocument(room: WishRoom) = room

    override fun documentHasId(room: WishRoom) = true

    override fun getDocumentId(room: WishRoom) = BsonString(room.id)
}