package models

/**
 * Created by rosexu on 15-08-31.
 */

case class User (
                  name: String,
                  team: Option[String])

case class Game (
                  id: String,
                  users: List[User])

object JsonFormats {

  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val userFormat = Json.format[User]
  implicit val gameFormat = Json.format[Game]
}