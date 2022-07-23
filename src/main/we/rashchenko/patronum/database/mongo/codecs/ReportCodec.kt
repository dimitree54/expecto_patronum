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
import we.rashchenko.patronum.database.UsersDatabase
import we.rashchenko.patronum.database.stats.Report

class ReportCodec(private val usersDatabase: UsersDatabase) : CollectibleCodec<Report> {
    private val documentCodec = DocumentCodec()

    override fun encode(
        bsonWriter: BsonWriter, report: Report, encoderContext: EncoderContext
    ) {
        val doc = Document()
        doc["_id"] = ObjectId(report.id)
        doc["senderId"] = ObjectId(report.sender.id)
        doc["receiverId"] = ObjectId(report.receiver.id)
        doc["message"] = report.message

        documentCodec.encode(bsonWriter, doc, encoderContext)
    }

    override fun decode(bsonReader: BsonReader, decoderContext: DecoderContext): Report {
        val doc = documentCodec.decode(bsonReader, decoderContext)
        return Report(
            id = (doc.getObjectId("_id")).toHexString(),
            sender = usersDatabase.get(doc.getObjectId("senderId").toHexString())!!,
            receiver = usersDatabase.get(doc.getObjectId("receiverId").toHexString())!!,
            message = doc.getString("message")
        )
    }

    override fun getEncoderClass() = Report::class.java

    override fun generateIdIfAbsentFromDocument(report: Report) = report

    override fun documentHasId(report: Report) = true

    override fun getDocumentId(report: Report) = BsonString(report.id)
}