package controllers

import play.api.mvc.{AnyContent, Action, Controller}

/**
 * Created by rosexu on 15-08-29.
 * @author Rose Xu
 */
class CreateGamePage extends Controller {
  def index = Action {
    Ok(views.html.creategame())
  }

  def storeGameKey = Action(parse.tolerantFormUrlEncoded) { request =>
    val name: String = getStringName(request.body.get("name").map(_.head))
    println(name)
    Ok(views.html.index())
  }

  def getStringName(option: Option[String]): String = {
    option match {
      case Some(name) => name
      case None => "error"
    }
  }
}
