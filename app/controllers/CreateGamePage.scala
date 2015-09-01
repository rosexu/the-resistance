package controllers

import javax.inject.Inject
import models.{User, Game}
import play.api.libs.json.Json

import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import scala.concurrent.ExecutionContext.Implicits.global

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

/**
 * Page Controller for the create game page. Responsible for generating and storing game key and the first player
 * Created by rosexu on 15-08-29.
 * @author Rose Xu
 */
class CreateGamePage @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents{

  import models.JsonFormats._

  def gameCollection: JSONCollection = db.collection[JSONCollection]("games")

  def index = Action {
    Ok(views.html.creategame())
  }

  def storeUserAndGameKey = Action.async(parse.tolerantFormUrlEncoded) { request =>
    val name: String = getStringName(request.body.get("name").map(_.head))
    val gameKey: String = generateGameKey
    val user1: User = User(name, None)
    val userList: List[User] = List(user1)
    val game: Game = Game(gameKey, userList)

    println(game.toString)

    val futureResult = gameCollection.insert(game)

    futureResult.map(_ => Ok(views.html.waiting(game, user1)))
  }

  def getStringName(option: Option[String]): String = {
    option match {
      case Some(name) => name
      case None => "error"
    }
  }

  def generateGameKey: String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val r = scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to 6) {
      val randomNum = r.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString()
  }
}
