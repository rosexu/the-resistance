package controllers

import javax.inject.Inject

import models._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.api.Cursor

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import utilities.optionalUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Page controller for the join game page
 * Created by rosexu on 15-09-01.
 */
class JoinGamePage @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
with MongoController with ReactiveMongoComponents{

  import models.JsonFormats._

  def gameCollection: JSONCollection = db.collection[JSONCollection]("games")
// is there a better way of doing this? currently dummy global object as keyholder
  var currentGame: Game = Game("1242", "waiting")
  var userCollection: JSONCollection = db.collection[JSONCollection]("users")

  def index = Action {
    Ok(views.html.joingame())
  }

  def joinGame = Action(parse.tolerantFormUrlEncoded) { request =>
    val keycode: String = getStringKeyCode(request.body.get("keycode").map(_.head))

    println(keycode)

    val futureList = retrieveGame(gameCollection, keycode)

    futureList onComplete{ list =>
      val game: Game = list.get.head
      currentGame = game
      println(currentGame)
    }

    Ok(views.html.creategame("/store-name-2"))
  }

  def storeUser = Action.async(parse.tolerantFormUrlEncoded) { request =>
    val name: String = optionalUtil.getStringName(request.body.get("name").map(_.head))
    val user1: User = User(name, None)

    userCollection = db.collection[JSONCollection]("users" + currentGame.id)

    val futureResult = userCollection.insert(user1)
    futureResult.map(_=> Ok(views.html.waiting(currentGame, user1)))
  }

  def retrieveGame(collection: JSONCollection, gameKey: String): Future[List[Game]] = {
    val cursor: Cursor[Game] = collection.find(Json.obj("id" -> gameKey)).cursor[Game]
    val futureList: Future[List[Game]] = cursor.collect[List]()
    return futureList
  }

  def getStringKeyCode(keycode: Option[String]): String = {
    keycode match {
      case Some(key) => key
      case None => "error"
    }
  }
}
