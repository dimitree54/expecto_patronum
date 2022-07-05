package we.rashchenko.patronum

import org.bson.types.ObjectId

class Tag(val name: String, val id: ObjectId = ObjectId.get())