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
import we.rashchenko.patronum.database.PatronUser
import we.rashchenko.patronum.database.stats.UserStats

class PatronUserCodec :
    CollectibleCodec<PatronUser> {
    private val documentCodec = DocumentCodec()

    override fun encode(
        bsonWriter: BsonWriter, user: PatronUser, encoderContext: EncoderContext
    ) {
        val doc = Document()
        doc["_id"] = ObjectId(user.id)
        doc["telegramId"] = user.telegramId
        doc["wishIdBlackList"] = user.wishIdBlackList.map { ObjectId(it) }
        doc["authorIdBlackList"] = user.authorIdBlackList.map { ObjectId(it) }

        doc["stats.reputation"] = user.stats.reputation
        doc["stats.reportsSent"] = user.stats.reportsSent
        doc["stats.reportsReceived"] = user.stats.reportsReceived
        doc["stats.myWishesActive"] = user.stats.myWishesActive
        doc["stats.myWishesDone"] = user.stats.myWishesDone
        doc["stats.myWishesCancelled"] = user.stats.myWishesCancelled
        doc["stats.othersWishesDone"] = user.stats.othersWishesDone
        doc["stats.othersWishesCancelled"] = user.stats.othersWishesCancelled

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): PatronUser {
        val doc = documentCodec.decode(bsonReader, decoderContext)
        return PatronUser(
            id = doc.getObjectId("_id").toHexString(),
            telegramId = doc.getLong("telegramId"),
            stats = UserStats(doc.getDouble("stats.reputation").toFloat()).apply {
                reportsSent = doc.getInteger("stats.reportsSent")
                reportsReceived = doc.getInteger("stats.reportsReceived")
                myWishesActive = doc.getInteger("stats.myWishesActive")
                myWishesDone = doc.getInteger("stats.myWishesDone")
                myWishesCancelled = doc.getInteger("stats.myWishesCancelled")
                othersWishesDone = doc.getInteger("stats.othersWishesDone")
                othersWishesCancelled = doc.getInteger("stats.othersWishesCancelled")
            },
        ).apply {
            wishIdBlackList.addAll(doc.getList("wishIdBlackList", ObjectId::class.java).map { it.toHexString() })
            authorIdBlackList.addAll(doc.getList("authorIdBlackList", ObjectId::class.java).map { it.toHexString() })
        }
    }

    override fun getEncoderClass() = PatronUser::class.java

    override fun generateIdIfAbsentFromDocument(user: PatronUser) = user

    override fun documentHasId(user: PatronUser) = true

    override fun getDocumentId(user: PatronUser) = BsonString(user.id)
}