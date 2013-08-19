package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

object Info extends Enumeration  {
	type Info = Value
	val UPDATE,FORECAST = Value
}

case class AlbumInfo(
  @Key("_id") id: ObjectId = new ObjectId,
  albumTitle:String,
  intro:String,
  videoChapter:Option[Int] = None,
  videoTitle:Option[String] = None,
  videoIntro:Option[String] = None,
  infoType:Info.Value=Info.UPDATE,
  author:String,
  added: Date= new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object AlbumInfo extends ModelCompanion[AlbumInfo, ObjectId] {
  val dao = new SalatDAO[AlbumInfo, ObjectId](collection = mongoCollection("AlbumInfos")) {}

  
}