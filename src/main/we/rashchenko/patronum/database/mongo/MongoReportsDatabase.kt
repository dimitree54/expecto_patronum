package we.rashchenko.patronum.database.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import we.rashchenko.patronum.database.ReportsDatabase
import we.rashchenko.patronum.database.stats.Report

class MongoReportsDatabase(
    private val reportsCollection: MongoCollection<Report>
) : ReportsDatabase {

    override fun new(report: Report) {
        reportsCollection.insertOne(report)
    }

    override fun cancel(report: Report) {
        reportsCollection.deleteOne(Filters.eq("_id", ObjectId(report.id)))
    }

    override fun generateNewReportId(): String {
        return ObjectId().toHexString()
    }
}