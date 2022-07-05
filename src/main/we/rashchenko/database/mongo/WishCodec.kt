package we.rashchenko.database.mongo

import com.mongodb.MongoClientSettings.getDefaultCodecRegistry
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import org.bson.types.ObjectId
import we.rashchenko.Wish

class WishCodec : CollectibleCodec<Wish> {
    private val documentCodec: Codec<Document>

    init {
        documentCodec = DocumentCodec(getDefaultCodecRegistry())
    }

    override fun encode(
        bsonWriter: BsonWriter, wish: Wish, encoderContext: EncoderContext
    ) {
        val wishDoc = Document()
        wishDoc["_id"] = wish.id
        wishDoc["author_id"] = wish.authorId
        wishDoc["tag_ids"] = wish.tagIds

        wish.title?.let { wishDoc["title"] = it }
        wish.description?.let { wishDoc["description"] = it }
        wish.image?.let { wishDoc["image"] = it }
        wish.wishArea?.let { wishDoc["wishArea"] = it }
        wish.expirationDate?.let { wishDoc["expirationDate"] = it }
        wish.patronId?.let { wishDoc["patron_id"] = it }

        documentCodec.encode(bsonWriter, wishDoc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): Wish {
        val wishDoc = documentCodec.decode(bsonReader, decoderContext)
        return Wish(wishDoc.getObjectId("author_id"), wishDoc.getObjectId("_id")).apply {
            wishDoc.getString("title")?.let { title = it }
            wishDoc.getString("description")?.let { description = it }
            wishDoc.getString("image")?.let { image = it }
            wishDoc.getDate("expirationDate")?.let { expirationDate = it }
            wishDoc.getObjectId("patron_id")?.let { patronId = it.toHexString() }
            wishDoc.getList("tag_ids", ObjectId::class.java)?.forEach { tagIds.add(it) }
        }
    }

    override fun getEncoderClass(): Class<Wish> {
        return Wish::class.java
    }

    override fun generateIdIfAbsentFromDocument(actor: Wish): Wish = actor

    override fun documentHasId(actor: Wish) = true

    override fun getDocumentId(actor: Wish): BsonString {
        check(documentHasId(actor)) { "This document does not have an " + "_id" }
        return BsonString(actor.id.toHexString())
    }
}