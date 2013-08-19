package controllers

import play.api._
import play.api.mvc._
import play.api.templates._

import models._
object Application extends Controller {
  
	def index = Action {
		Ok(views.html.index(UserCenter.userLoginForm))
	}

	def adminIndex = Action{
		Ok(views.html.login.adminLogin(UserCenter.userLoginForm,""))
	}

	def intMenu = Action{ request =>
		val username = request.session.get("admin_username")
		Logger.info("init Menu:	"+username)
		Logger.info("init Menu:	"+request.session.get("admin_disname"))
		if(username.isDefined){
			val user = User.findOneByUsername(username.get).get 

			Ok(views.html.admin.adminHome()(request.session))
		}else
			Ok(views.html.login.adminLogin(UserCenter.userLoginForm,"用户不存在，请重新登录。"))

	}

    trait Secured {
		private def login(request: RequestHeader) = request.session.get("username")
		private def adminLogin(request: RequestHeader) = request.session.get("admin_username")

		private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index)

		def isAuthed(f: => String => Request[AnyContent] => Result) =  Security.Authenticated(login, onUnauthorized) {
		    user => Action(request => f(user)(request))
		}

		def adminIsAuthed(f: => String => Request[AnyContent] => Result) =  Security.Authenticated(adminLogin, onUnauthorized) {
		    user => Action(request => f(user)(request))
		}

	}
}