package we.rashchenko.patronum.database.mongo

import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import we.rashchenko.patronum.User

class UserCodec : CollectibleCodec<User> {
    private val documentCodec: Codec<Document>

    init {
        documentCodec = DocumentCodec()
    }

    override fun encode(
        bsonWriter: BsonWriter, user: User, encoderContext: EncoderContext
    ) {
        val userDoc = Document()
        userDoc["_id"] = user.id
        userDoc["name"] = user.name
        documentCodec.encode(bsonWriter, userDoc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): User {
        val userDoc = documentCodec.decode(bsonReader, decoderContext)
        return User(userDoc.getString("name"), userDoc.getObjectId("_id"))
    }

    override fun getEncoderClass(): Class<User> {
        return User::class.java
    }

    override fun generateIdIfAbsentFromDocument(user: User): User = user

    override fun documentHasId(user: User) = true

    override fun getDocumentId(user: User): BsonString {
        check(documentHasId(user)) { "This document does not have an " + "_id" }
        return BsonString(user.id.toHexString())
    }
}