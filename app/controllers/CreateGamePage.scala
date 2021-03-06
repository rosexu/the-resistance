package controllers

import javax.inject.Inject
import models.{User, Game}
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json

import play.api.mvc.{Action, Controller}
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import utilities.optionalUtil
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
  var userCollection: JSONCollection = db.collection[JSONCollection]("users")

//  TODO: close the websocket when you start game
  /**
   * End point for web socket
   * @return Iteratee
   */
  def socket = WebSocket.using[String] { request =>

    // Log events to the console
    val in = Iteratee.foreach[String](println).map { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    (in, out)
  }

  /**
   * End point for the Create Game Page
   * @return a page template prompting user to enter name
   */
  def index = Action {
    Ok(views.html.creategame("/store-name"))
  }

  /**
   * Create Game and User object and store those objects in the database. Triggered
   * after the user enters her name and presses enter.
   * @return the waiting page where the user wait for other players to join page
   */
  def storeUserAndGameKey = Action.async(parse.tolerantFormUrlEncoded) { request =>
    val name: String = optionalUtil.getStringName(request.body.get("name").map(_.head))
    gameKey = generateGameKey
    userCollection = db.collection[JSONCollection]("users" + gameKey)
    val user1: User = User(name, None)
    val game: Game = Game(gameKey, "Waiting")

    println(game.toString)

    val futureResult = gameCollection.insert(game)
    val futureResult2 = userCollection.insert(user1)

    messageCollection = db.collection[BSONCollection]("messages" + gameKey)

    val waitForBoth = for {
      fr <- futureResult
      fr2 <- futureResult2
    } yield (fr, fr2)

    waitForBoth.map(_=> startTailableCursor())
    waitForBoth.map(_ => Ok(views.html.waiting(game, user1, false)))
  }

  /**
   * Start the pub/sub system using capped collection called messages<gamekey>
   * and tailable cursor in MongoDB.
   */
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

  /**
   * Tester endpoint to see if pub/sub system works
   * @return Http Ok
   */
  def addPlayer = Action {
    val doc = BSONDocument("message" -> "add new player")
    messageCollection.insert(doc)
    Ok
  }

  /**
   * Build a random six letter game key with lower and upper case
   * letters as well as numbers
   * @return String: six letter game key
   */
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
