package we.rashchenko

import org.bson.types.ObjectId

class User(val name: String, val id: ObjectId = ObjectId.get())