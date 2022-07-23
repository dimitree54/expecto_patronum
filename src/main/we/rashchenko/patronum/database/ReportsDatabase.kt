package we.rashchenko.patronum.database

import we.rashchenko.patronum.database.stats.Report

interface ReportsDatabase {
    fun new(report: Report)
    fun cancel(report: Report)
    fun generateNewReportId(): String
}