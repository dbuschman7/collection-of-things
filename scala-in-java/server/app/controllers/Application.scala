package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  case class BodyData(body: String)

  def echo = Action { implicit request =>
    val form = Form(mapping("body" -> nonEmptyText)(BodyData.apply)(BodyData.unapply))
    println(form.bindFromRequest)
    Ok("Server - message sent")
  }

}