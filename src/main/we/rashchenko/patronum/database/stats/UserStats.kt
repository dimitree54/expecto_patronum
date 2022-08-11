package we.rashchenko.patronum.database.stats

import java.util.*
import kotlin.math.roundToInt

class UserStats(var reputation: Int) {
    var reportsSent: Int = 0
    var reportsReceived: Int = 0
    var myWishesActive: Int = 0
    var myWishesDone: Int = 0
    var myWishesCancelled: Int = 0
    var myFulfillmentCancelled: Int = 0
    var othersWishesDone: Int = 0
    var othersFulfillmentCancelled: Int = 0

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

    fun myFulfillmentCancel() {
        myFulfillmentCancelled++
        reputation += ReputationDeltas.myFulfillmentCancel
    }

    fun othersWishDone(rate: Float = 1f, bounty: Int = 0) {
        othersWishesDone++
        reputation += (ReputationDeltas.othersWishDoneMax * rate).roundToInt() + bounty
    }

    fun othersFulfillmentCancel() {
        othersFulfillmentCancelled++
        reputation += ReputationDeltas.othersFulfillmentCancel
    }

    fun stakeBounty(bounty: Int) {
        reputation -= bounty
    }

    private object ReputationDeltas {
        val sendReport: Int
        val receiveReport: Int
        val myNewWish: Int
        val myWishDone: Int
        val myWishCancel: Int
        val myFulfillmentCancel: Int
        val othersWishDoneMax: Int
        val othersFulfillmentCancel: Int

        init {
            Properties().also {
                it.load(ClassLoader.getSystemResourceAsStream("reputation.properties"))
                sendReport = (it["send_report"] as String).toInt()
                receiveReport = (it["receive_report"] as String).toInt()
                myNewWish = (it["my_new_wish"] as String).toInt()
                myWishDone = (it["my_wish_done"] as String).toInt()
                myWishCancel = (it["my_wish_cancel"] as String).toInt()
                myFulfillmentCancel = (it["my_fulfillment_cancel"] as String).toInt()
                othersWishDoneMax = (it["others_wish_done_max"] as String).toInt()
                othersFulfillmentCancel = (it["others_fulfillment_cancel"] as String).toInt()
            }
        }
    }
}