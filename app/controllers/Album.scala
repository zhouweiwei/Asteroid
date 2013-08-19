package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import models._
import com.mongodb.casbah.Imports._
import java.util.Date

/**
* Manage a database of albums
*/
object Album extends Controller with Application.Secured{
	/**
	* This result directly redirect to the application home.
	*/
	val Home = Redirect(routes.Album.list(0, 2, ""))

	/**
	* Describe the album form (used in both edit and create screens).
	*/ 
	def albumForm(id: ObjectId = new ObjectId,author:String) = Form(
	    mapping(
	      "id" -> ignored(id),
	      "title" -> nonEmptyText,
	      "intro" -> nonEmptyText,
	      "additional" -> text,
	      "author"-> ignored(author),	      
	      "heat" -> ignored(0),
	      "videoNum" ->  of[Int],
	      "upNum" ->ignored(0),
	      "added" -> ignored(new Date()),
	      "updated" -> optional(of[Date]),
	      "deleted" -> optional(of[Date])
	    
	    )(models.Album.apply)(models.Album.unapply)
	  )

	/**
	* Handle default path requests, redirect to computers list
	*/ 
	def listAlbum = adminIsAuthed { adminLogin => implicit request =>	
		Home
	}

	/**
	* Display the paginated list of albums.
	*
	* @param page Current page number (starts from 0)
	* @param orderBy Column to be sorted
	* @param filter Filter applied on album title
	*/
	def list(page: Int, orderBy: Int, filter: String) = adminIsAuthed { adminLogin => implicit request =>
    	Ok(views.html.album.list(
      		models.Album.listAdmin(page = page, orderBy = orderBy, filter = filter,author=request.session.get("admin_username").get),orderBy, filter)(request.session,request.flash )
    	)
  	}

    /**
    * Display the 'new Album form'.
    */
    def create = adminIsAuthed { adminLogin => implicit request =>
    	Ok(views.html.album.create(albumForm(author=request.session.get("admin_username").get))(request.session))
  	}

  	/**
    * Handle the 'new album form' submission.
    */
    def save = adminIsAuthed { adminLogin => implicit request =>
	    albumForm(author=request.session.get("admin_username").get).bindFromRequest.fold(
	      formWithErrors => BadRequest(views.html.album.create(formWithErrors)(request.session)),
	      album => {
	      	//album.author = request.session.get("admin_username")
	        models.Album.insert(album)
	        val info = models.AlbumInfo(
	        	albumTitle = album.title,
	        	intro = album.intro,
	        	author = album.author,
	        	infoType = Info.FORECAST
	        	)
	        models.AlbumInfo.insert(info)
	        Home.flashing("success" -> "Album %s has been created".format(album.title))
	      }
	    )
	}

	/**
	* Display the 'edit form' of a existing Album.
	*
	* @param id Id of the Album to edit
	*/
	def edit(id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
	    models.Album.findOneById(id).map { album =>
	    	Logger.info("edit album:	"+album.title)
	      	Ok(views.html.album.edit(id, albumForm(id,author=request.session.get("admin_username").get).fill(album)))
	    }.getOrElse(NotFound)
	}

	/**
	* Handle the 'edit form' submission 
	*
	* @param id Id of the computer to edit
	*/
	def update(id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
		albumForm(author=request.session.get("admin_username").get).bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.album.edit(id, formWithErrors)),
				album => {
					val old_album = models.Album.findOneById(id).get
					models.Album.save(old_album.copy(
							title = album.title,
							intro = album.intro,
							additional = album.additional,
							videoNum = album.videoNum,
							updated = Some(new Date())
						)
					)
					Home.flashing("success" -> "Album %s has been updated".format(album.title))
				}
		)
	}

	/**
	* Handle album deletion.
	*/
	def delete(id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
		models.Album.remove(MongoDBObject("_id" -> id))
		Home.flashing("success" -> "Album has been deleted")
	}

	/**
	* Handle album detail info.
	*/
	def detail(id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
		Redirect(routes.VideoCenter.list(id=id))
	}

}