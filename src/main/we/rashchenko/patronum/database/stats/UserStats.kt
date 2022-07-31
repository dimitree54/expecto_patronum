package we.rashchenko.patronum.database.stats

import java.util.*

class UserStats(var reputation: Float) {
    var reportsSent: Int = 0
    var reportsReceived: Int = 0
    var myWishesActive: Int = 0
    var myWishesDone: Int = 0
    var myWishesCancelled: Int = 0
    var othersWishesDone: Int = 0
    var othersWishesCancelled: Int = 0

    fun sendReport() {
        reportsSent++
        reputation += ReputationDeltas.sendReport
    }

    fun receiveReport() {
        reportsReceived++
        reputation += ReputationDeltas.receiveReport
    }

    fun myNewWish() {
        myWishesActive++
        reputation += ReputationDeltas.myNewWish
    }

    fun myWishDone() {
        myWishesActive--
        myWishesDone++
        reputation += ReputationDeltas.myWishDone
    }

    fun myWishCancel() {
        myWishesActive--
        myWishesCancelled++
        reputation += ReputationDeltas.myWishCancel
    }

    fun othersWishDone(rate: Float = 1f, bounty: Float = 0f) {
        othersWishesDone++
        reputation += ReputationDeltas.othersWishDoneMax * rate + bounty
    }

    fun othersWishCancel() {
        othersWishesCancelled++
        reputation += ReputationDeltas.othersWishCancel
    }

    fun stakeBounty(bounty: Float) {
        reputation -= bounty
    }

    private object ReputationDeltas {
        val sendReport: Float
        val receiveReport: Float
        val myNewWish: Float
        val myWishDone: Float
        val myWishCancel: Float
        val othersWishDoneMax: Float
        val othersWishCancel: Float

        init {
            Properties().also {
                it.load(ClassLoader.getSystemResourceAsStream("reputation.properties"))
                sendReport = (it["send_report"] as String).toFloat()
                receiveReport = (it["receive_report"] as String).toFloat()
                myNewWish = (it["my_new_wish"] as String).toFloat()
                myWishDone = (it["my_wish_done"] as String).toFloat()
                myWishCancel = (it["my_wish_cancel"] as String).toFloat()
                othersWishDoneMax = (it["others_wish_done_max"] as String).toFloat()
                othersWishCancel = (it["others_wish_cancel"] as String).toFloat()
            }
        }
    }
}