package we.rashchenko.patronum

import org.bson.types.ObjectId

class User(val telegramId: Long, val id: ObjectId = ObjectId.get()){
    var score: Double = 0.0
    val wishBlackList = mutableListOf<ObjectId>()
    val userBlackList = mutableListOf<ObjectId>()
}