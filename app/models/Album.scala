package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

case class Album(
  @Key("_id") id: ObjectId = new ObjectId,
  title:String,
  intro:String,
  additional:String="",
  author: String, 
  heat:Int=0,
  videoNum:Int=0,
  upNum:Int=0,
  added: Date = new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object Album extends ModelCompanion[Album, ObjectId] {
  val dao = new SalatDAO[Album, ObjectId](collection = mongoCollection("Albums")) {}

  val columns = List("dump","_id", "title", "intro", "additional", "heat")

  def list(page: Int = 0, pageSize: Int = 5, orderBy: Int = 2, filter: String = ""): Page[Album] = {

    val where = if(filter == "") MongoDBObject.empty else MongoDBObject("title" ->(""".*"""+filter+""".*""").r)
    val ascDesc = if(orderBy > 0) 1 else -1
    val order = MongoDBObject(columns(orderBy.abs) -> ascDesc)

    val totalRows = count(where);
    val offset = pageSize * page
    val albums = find(where).sort(order).limit(pageSize).skip(offset).toSeq

    Page(albums, page, offset, totalRows)
  }

  def listAdmin(page: Int = 0, pageSize: Int = 5, orderBy: Int = 1, filter: String = "",author:String): Page[Album] = {

    val where = if(filter == "") MongoDBObject("author"->author) else MongoDBObject("title" ->(""".*"""+filter+""".*""").r,"author"->author)
    val ascDesc = if(orderBy > 0) 1 else -1
    val order = MongoDBObject(columns(orderBy.abs) -> ascDesc)

    val totalRows = count(where);
    val offset = pageSize * page
    val albums = find(where).sort(order).limit(pageSize).skip(offset).toSeq

    Page(albums, page, offset, totalRows)
  }

  def options(author:String): Seq[(String,String)] = {
    find(MongoDBObject("author"->author)).map(it => (it.id.toString, it.title)).toSeq
  }
}




