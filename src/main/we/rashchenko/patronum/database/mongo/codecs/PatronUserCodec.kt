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
        doc["wishIdStopList"] = user.wishIdStopList.map { ObjectId(it) }
        doc["userIdStopList"] = user.userIdStopList.map { ObjectId(it) }
        user.languageCode ?.let { doc["languageCode"] = it }

        doc["statsReputation"] = user.stats.reputation
        doc["statsReportsSent"] = user.stats.reportsSent
        doc["statsReportsReceived"] = user.stats.reportsReceived
        doc["statsMyWishesActive"] = user.stats.myWishesActive
        doc["statsMyWishesDone"] = user.stats.myWishesDone
        doc["statsMyWishesCancelled"] = user.stats.myWishesCancelled
        doc["statsOthersWishesDone"] = user.stats.othersWishesDone
        doc["statsOthersFulfillmentCancelled"] = user.stats.othersFulfillmentCancelled

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): PatronUser {
        val doc = documentCodec.decode(bsonReader, decoderContext)
        return PatronUser(
            id = doc.getObjectId("_id").toHexString(),
            telegramId = doc.getLong("telegramId"),
            stats = UserStats(doc.getInteger("statsReputation")).apply {
                reportsSent = doc.getInteger("statsReportsSent")
                reportsReceived = doc.getInteger("statsReportsReceived")
                myWishesActive = doc.getInteger("statsMyWishesActive")
                myWishesDone = doc.getInteger("statsMyWishesDone")
                myWishesCancelled = doc.getInteger("statsMyWishesCancelled")
                othersWishesDone = doc.getInteger("statsOthersWishesDone")
                othersFulfillmentCancelled = doc.getInteger("statsOthersFulfillmentCancelled")
            },
        ).apply {
            wishIdStopList.addAll(doc.getList("wishIdStopList", ObjectId::class.java).map { it.toHexString() })
            userIdStopList.addAll(doc.getList("userIdStopList", ObjectId::class.java).map { it.toHexString() })
            doc.getString("languageCode")?.let { languageCode = it }
        }
    }

    override fun getEncoderClass() = PatronUser::class.java

    override fun generateIdIfAbsentFromDocument(user: PatronUser) = user

    override fun documentHasId(user: PatronUser) = true

    override fun getDocumentId(user: PatronUser) = BsonString(user.id)
}