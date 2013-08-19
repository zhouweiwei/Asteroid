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
object VideoCenter extends Controller with Application.Secured{

	/**
	* Describe the album form (used in both edit and create screens).
	*/ 
	import play.Play

	def videoForm(id: ObjectId = new ObjectId ,album_id :ObjectId ) = Form(
	    mapping(
	      "id" -> ignored(id),
	      "album" -> ignored(album_id),
	      "chapter" -> of[Int],
	      "name" -> nonEmptyText,
	      "fileName"->optional(of[String]),
	     // "path"->  ignored(Play.application.path+"/data/"),
	      "path"->  ignored("data/"),
	      "additional" -> text,
	      "intro"-> nonEmptyText,
	      "added" -> ignored(new Date()),
	      "updated" -> optional(of[Date]),
	      "deleted" -> optional(of[Date])
	      
  
	    )(models.Video.apply)(models.Video.unapply)
	  )


	/**
	* Display the paginated list of albums.
	*
	* @param page Current page number (starts from 0)
	* @param orderBy Column to be sorted
	* @param filter Filter applied on album title
	*/
	def list(id: ObjectId, page: Int, orderBy: Int, filter: String) = adminIsAuthed { adminLogin => implicit request =>
    	Ok(views.html.videos.list(
    		models.Album.findOneById(id).get,
      		models.Video.list(page = page, orderBy = orderBy, filter = id.toString),orderBy, filter)
    	)
  	}

    /**
    * Display the 'new Video form'.
    */
    def create(album_id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
    	Ok(views.html.videos.create(videoForm(album_id=album_id),album_id)(request.session))
  	}

  	/**
    * Handle the 'new video form' submission.
    */
    def save(album_id: ObjectId) = Action(parse.multipartFormData){  implicit request =>
	   // Logger.info("upload video: album_id "+album_id)
	    videoForm(album_id=album_id).bindFromRequest.fold(
	      	formWithErrors => BadRequest(views.html.videos.create(formWithErrors,album_id)(request.session)),
	      	video => {
	      		//album.author = request.session.get("admin_username")
	        	Logger.info("upload video: album_id "+video.album)
	        	var newVideo : models.Video =  video

	        	import java.io.File
	    		request.body.file("video").map { video =>
					
					val album = models.Album.findOneById(newVideo.album).get
					val filename = video.filename
					val contentType = video.contentType.get
					val videoType = filename.split('.').last

					val path = newVideo.path + album.author+"/"+album.id+"/"+newVideo.id+"."+videoType
					Logger.info("upload video: filename "+filename)
					Logger.info("upload video: contentType "+contentType)
					Logger.info("upload video: videoType "+videoType)
					Logger.info("upload video: path "+path)
					video.ref.moveTo(new File(path),true)
				
					models.Video.insert(newVideo.copy(path=path,fileName=Some(filename)))
				
					val info = models.AlbumInfo(
						albumTitle = album.title,
						intro = album.intro,
						videoChapter = Some(newVideo.chapter),
						videoTitle = Some(newVideo.name),
						videoIntro = Some(newVideo.intro),
						author = album.author,
						infoType = Info.UPDATE
					)
					models.AlbumInfo.insert(info)
				}
			
				Redirect(routes.VideoCenter.list(id=newVideo.album)).flashing("success" -> "Video %s has been created".format(newVideo.name))

			}
	    )   	
	}

	/**
	* Display the 'edit form' of a existing Video.
	*
	* @param id Id of the Video to edit
	*/
	def edit(id: ObjectId) = adminIsAuthed { adminLogin => implicit request =>
	    models.Video.findOneById(id).map { video =>
	    	Logger.info("edit video:	"+video.name)
	    	val author = request.session.get("admin_username").get
	      	Ok(views.html.videos.edit(id, video.album, videoForm(id=id,album_id=video.album).fill(video),models.Album.options(author)))
	    }.getOrElse(NotFound)
	}

	/**
	* Handle the 'edit form' submission 
	*
	* @param id Id of the computer to edit
	*/
	def update(albumId:ObjectId,videoId:ObjectId) = Action(parse.multipartFormData){  implicit request =>
		val author = request.session.get("admin_username").get
		Logger.info("edit video: video "+videoId)
		
		val data = request.body.asFormUrlEncoded
		val dataMap = data.asInstanceOf[Map[String,Seq[String]]]
		val album_id = dataMap("album").mkString 
		val editAlbumId =
			if(album_id.equals("")){
				albumId
			}else{
				new ObjectId(album_id)
			}
		//Logger.info("edit video: form   "+dataMap)
		Logger.info("edit video: form album_id  "+editAlbumId)
		videoForm(album_id=editAlbumId).bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.videos.edit(videoId, albumId,formWithErrors , models.Album.options(author))),
				video => {
					val old_video = models.Video.findOneById(videoId).get
					//Logger.info("edit video: album "+video)
					
					var fileName = old_video.fileName
					import java.io.File
					val newPath =
		    		request.body.file("video").map { videofile =>
						
						val album = models.Album.findOneById(video.album).get
						val filename = videofile.filename
						val contentType = videofile.contentType.get
						val videoType = filename.split('.').last

						val path = video.path + album.author+"/"+album.id+"/"+old_video.id+"."+videoType
						Logger.info("edit video: filename "+filename)
						Logger.info("edit video: contentType "+contentType)
						Logger.info("edit video: videoType "+videoType)
						Logger.info("edit video: path "+path)
						videofile.ref.moveTo(new File(path),true)
						
						fileName = Some(filename)
						path
					}.getOrElse{
						old_video.path
					}
					Logger.info("edit video: path "+newPath)
					models.Video.save(old_video.copy(
							album = editAlbumId,
							chapter = video.chapter,
							name = video.name,
							fileName = fileName,
							intro = video.intro,
							additional = video.additional,
							path = newPath,
							updated = Some(new Date())
						)
					)
					Redirect(routes.VideoCenter.list(id=albumId)).flashing("success" -> "Video %s has been updated".format(video.name))
				}
		)
	}

	/**
	* Handle Video deletion.
	*/
	def delete(album:ObjectId,video:ObjectId) = adminIsAuthed { adminLogin => implicit request =>
		models.Video.remove(MongoDBObject("_id" -> video))
		Redirect(routes.VideoCenter.list(id=album)).flashing("success" -> "Album has been deleted")
	}

	/**
	* Handle Video detail info.
	*/
	def detail(videoId:ObjectId) = adminIsAuthed { adminLogin => implicit request =>
		val video = models.Video.findOneById(videoId).get

		Ok(views.html.videos.player(video))
	}

}