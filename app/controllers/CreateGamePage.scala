package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

/**
 * Created by rosexu on 15-08-29.
 * @author Rose Xu
 */
class CreateGamePage @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents{
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
