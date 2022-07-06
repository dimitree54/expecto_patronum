package we.rashchenko.patronum.database

import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.*
import org.bson.types.ObjectId
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
        userDoc["telegramId"] = user.telegramId
        userDoc["score"] = user.score
        userDoc["wishBlackList"] = user.wishBlackList
        userDoc["userBlackList"] = user.userBlackList
        documentCodec.encode(bsonWriter, userDoc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): User {
        val userDoc = documentCodec.decode(bsonReader, decoderContext)
        return User(userDoc.getLong("telegramId"), userDoc.getObjectId("_id")).apply {
            score = userDoc.getDouble("score")
            wishBlackList.addAll(userDoc.getList("wishBlackList", ObjectId::class.java))
            userBlackList.addAll(userDoc.getList("userBlackList", ObjectId::class.java))
        }
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