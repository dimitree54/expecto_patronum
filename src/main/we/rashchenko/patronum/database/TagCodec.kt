package we.rashchenko.patronum.database

import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import we.rashchenko.patronum.Tag

class TagCodec : CollectibleCodec<Tag> {
    private val documentCodec: Codec<Document>

    init {
        documentCodec = DocumentCodec()
    }

    override fun encode(
        bsonWriter: BsonWriter, tag: Tag, encoderContext: EncoderContext
    ) {
        val tagDoc = Document()
        tagDoc["_id"] = tag.id
        tagDoc["name"] = tag.name
        documentCodec.encode(bsonWriter, tagDoc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): Tag {
        val userDoc = documentCodec.decode(bsonReader, decoderContext)
        return Tag(userDoc.getString("name"), userDoc.getObjectId("_id"))
    }

    override fun getEncoderClass(): Class<Tag> {
        return Tag::class.java
    }

    override fun generateIdIfAbsentFromDocument(tag: Tag): Tag = tag

    override fun documentHasId(tag: Tag) = true

    override fun getDocumentId(tag: Tag): BsonString {
        check(documentHasId(tag)) { "This document does not have an " + "_id" }
        return BsonString(tag.id.toHexString())
    }
}