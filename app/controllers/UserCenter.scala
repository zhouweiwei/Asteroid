package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models._
object UserCenter extends Controller with Application.Secured{
  
    val userLoginForm = Form(
	    tuple(
	      "username" -> text,
	      "passwd" -> text 
	    ) verifying("无效的用户名、密码", result => result match {
	      case (username, passwd) => User.authenticate(username, passwd).isDefined
	      })
	  )

    def userLogin = Action { implicit request =>
  		Logger.info("userLogin"+request.path)
  		userLoginForm.bindFromRequest.fold(
		      formWithErrors => BadRequest(views.html.index(formWithErrors)),
		      user => {
		        Logger.info("Login:..username:"+ user._1)
		        Logger.info("Login:..passwd:"+ user._2)
		        Ok(views.html.mystudy.myvideo("OK")).withSession(
		        					"username" -> user._1
		        					)   
		      }
		    )
  			    
	  }
  	
  	def adminLogin = Action {implicit request =>
  		Logger.info("adminLogin"+request.path)
  		userLoginForm.bindFromRequest.fold(
		      formWithErrors => BadRequest(views.html.login.adminLogin(formWithErrors,"")),
		      user => {
		        Logger.info("Login:..username:"+ user._1)
		        Logger.info("Login:..passwd:"+ user._2)
		        User.findOneByUsername(user._1) match {
		        	case Some(admin) => {
		        		if(admin.role != Role.Normal){
		        			Redirect(routes.Application.intMenu)
		        				.withSession(
		        					"admin_username" -> admin.username
		        					,"admin_disname" -> (admin.nickname+"@["+admin.role+"]")) 
		        		}
		        		else Ok(views.html.login.adminLogin(userLoginForm,"该用户不具有管理权限。")) 
		        		// Logger.info("-----------"+admin.role)
		        	}
		        		
		        	case _ => 	Ok(views.html.login.adminLogin(userLoginForm,"用户不存在，请重新登录。")) 
		        }
		        
		         
		      }
		    )
  	}


  // def initHomePage = Action{
    	
  //  }
  	// def loginAction(f: Request[AnyContent]=> Result):Action[AnyContent] = {
  	// 	Action{ implicit request =>
  	// 		Logger.info("Calling loginAction")
  	// 		userLoginForm.bindFromRequest.fold(
		 //      formWithErrors => BadRequest(views.html.index(formWithErrors)),
		 //      user => {
		 //        Logger.info("Login:..username:"+ user._1)
		 //        Logger.info("Login:..passwd:"+ user._2)
		        
		 //      }
		 //    )
  	// 		f(request)
  	// 	}
  		
  	//}
  	
}