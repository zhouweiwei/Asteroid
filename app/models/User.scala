package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._

import mongoContext._

case class User(
  @Key("_id") id: ObjectId = new ObjectId,
  username: String,
  password: String,
  nickname: String,
  role:Role.Value = Role.Normal,
  added: Date = new Date(),
  updated: Option[Date] = None,
  deleted: Option[Date] = None
 
)

object User extends ModelCompanion[User, ObjectId] {
  val dao = new SalatDAO[User, ObjectId](collection = mongoCollection("Users")) {}

  def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))
  
  def authenticate(username: String, passwd: String): Option[User] = {
    // println("login_id:" + login_id + " passwd: " + passwd + " authority: " + authority)
    println("[DAO]  models.User - "+username)
    val r: Option[User] = dao.findOne(MongoDBObject("username" -> username, "password" -> passwd))
    r
  }
}