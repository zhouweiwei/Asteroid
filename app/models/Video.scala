package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

case class Video(
  @Key("_id") id: ObjectId = new ObjectId,
  @Key("album_id")album: ObjectId ,
  chapter:Int,
  name:String,
  fileName:Option[String] = None,
  path:String,
  intro:String,
  additional:String="",
  added: Date = new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object Video extends ModelCompanion[Video, ObjectId] {
  val dao = new SalatDAO[Video, ObjectId](collection = mongoCollection("Videos")) {}

  val columns = List("intro","_id", "chapter", "name", "additional","added")

  def list(page: Int = 0, pageSize: Int = 5, orderBy: Int = 1, filterName:String = "album_id", filter: String = ""): Page[Video] = {

    val where = if(filterName == "album_id") MongoDBObject(filterName -> new ObjectId(filter)) else MongoDBObject(filterName ->(""".*"""+filter+""".*""").r)
    val ascDesc = if(orderBy > 0) 1 else -1
    val order = MongoDBObject(columns(orderBy.abs) -> ascDesc)
    println("[DAO]  models.Video - "+where)
    val totalRows = count(where);
    val offset = pageSize * page
    val videos = find(where).sort(order).limit(pageSize).skip(offset).toSeq

    Page(videos, page, offset, totalRows)
  }
  
}