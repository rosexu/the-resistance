package controllers

import javax.inject.Inject
import models.{User, Game}
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json

import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext.Implicits.global

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import scala.concurrent.Future

/**
 * Page Controller for the create game page. Responsible for generating and storing game key and the first player
 * Created by rosexu on 15-08-29.
 * @author Rose Xu
 */
class CreateGamePage @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents{

  import models.JsonFormats._

  var gameKey: String = "";
  def gameCollection: JSONCollection = db.collection[JSONCollection]("games")
  var messageCollection: BSONCollection = db.collection[BSONCollection]("messages")

  def index = Action {
    Ok(views.html.creategame())
  }

  def storeUserAndGameKey = Action.async(parse.tolerantFormUrlEncoded) { request =>
    val name: String = getStringName(request.body.get("name").map(_.head))
    gameKey = generateGameKey
    val user1: User = User(name, None)
    val userList: List[User] = List(user1)
    val game: Game = Game(gameKey, userList)

    println(game.toString)

    val futureResult = gameCollection.insert(game)

    messageCollection = db.collection[BSONCollection]("messages" + gameKey)

    futureResult.map(_=> startTailableCursor())
    futureResult.map(_ => Ok(views.html.waiting(game, user1)))
  }

  def startTailableCursor(): Unit = {
    val fun: Future[Unit] = messageCollection.createCapped(1000, None)
    fun onComplete{ _ =>
      val doc = BSONDocument("message" -> "init")
      messageCollection.insert(doc)
      println("open tailable cursor")
      val cursor = messageCollection
        .find(BSONDocument())
        .options(QueryOpts().tailable.awaitData)
        .cursor()
      cursor.enumerate().apply(Iteratee.foreach {
        doc => println("Document inserted: " + doc.toString())
      })
    }
  }

  def addPlayer = Action {
    val doc = BSONDocument("message" -> "add new player")
    messageCollection.insert(doc)
    Ok
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
