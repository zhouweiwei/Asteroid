package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

case class Tag(
  @Key("_id") id: ObjectId = new ObjectId,
  name:String,
  @Key("user_id")user: Option[ObjectId] = None,
  @Key("album_id")album: Option[ObjectId] = None,
  @Key("video_id")video: Option[ObjectId] = None,
  added: Date = new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object Tag extends ModelCompanion[Tag, ObjectId] {
  val dao = new SalatDAO[Tag, ObjectId](collection = mongoCollection("Tags")) {}

  
}