package controllers

import javax.inject.Inject

import models._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.api.{QueryOpts, Cursor}

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
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
  var currentUser: User = User("admin", None)
  var userCollection: JSONCollection = db.collection[JSONCollection]("users")
  var messageCollection: BSONCollection = db.collection[BSONCollection]("messages")

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
    currentUser = user1

    userCollection = db.collection[JSONCollection]("users" + currentGame.id)
    messageCollection = db.collection[BSONCollection]("messages" + currentGame.id)
    val otherUsers: Future[List[User]] = userCollection.find(Json.obj()).cursor[User].collect[List]()
    val futureResult = userCollection.insert(user1)

    val twoFut = for{
      f1Result <- otherUsers
      f2Result <- futureResult
    } yield (f1Result, f2Result)

    twoFut.map(_ => startTailableCursor())
    twoFut.map(thing =>
      Ok(views.html.waiting(currentGame, user1, true))
    )
  }

  def retrieveGame(collection: JSONCollection, gameKey: String): Future[List[Game]] = {
    val cursor: Cursor[Game] = collection.find(Json.obj("id" -> gameKey)).cursor[Game]
    val futureList: Future[List[Game]] = cursor.collect[List]()
    return futureList
  }

  def getAllOtherUsers = Action.async {
    val allUsers = userCollection.find(Json.obj("name" -> Json.obj("$ne" -> currentUser.name))).cursor[User].collect[List]()
    allUsers.map(users => Ok(Json.toJson(users)))
  }

  def getStringKeyCode(keycode: Option[String]): String = {
    keycode match {
      case Some(key) => key
      case None => "error"
    }
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
}
