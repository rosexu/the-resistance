package controllers

import javax.inject.Inject

import models.{Game, User}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}

/**
 * Page controller for the join game page
 * Created by rosexu on 15-09-01.
 */
class JoinGamePage @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
with MongoController with ReactiveMongoComponents{

  def index = Action {
    Ok(views.html.joingame())
  }

  def joinGame = Action(parse.tolerantFormUrlEncoded) { request =>
    val keycode: String = getStringKeyCode(request.body.get("keycode").map(_.head))

    println(keycode)

    Ok(views.html.index())
  }

  def getStringKeyCode(keycode: Option[String]): String = {
    keycode match {
      case Some(key) => key
      case None => "error"
    }
  }
}
