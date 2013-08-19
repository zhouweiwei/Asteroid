package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

case class Notes(
  @Key("_id") id: ObjectId = new ObjectId,
  @Key("user_id")user: Option[ObjectId] = None,
  @Key("video_id")video: Option[ObjectId] = None,
  content:String,
  added: Date = new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object Notes extends ModelCompanion[Notes, ObjectId] {
  val dao = new SalatDAO[Notes, ObjectId](collection = mongoCollection("Notes")) {}

  
}