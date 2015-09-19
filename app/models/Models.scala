package models

/**
 * Created by rosexu on 15-08-31.
 */

case class User (
                  name: String,
                  team: Option[String])

case class Game (
                  id: String,
                  status: String)

object JsonFormats {

  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val userFormat = Json.format[User]
  implicit val gameFormat = Json.format[Game]
}

import akka.actor._

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}