package utilities

/**
 * Created by rosexu on 15-09-15.
 */
object optionalUtil {
  def getStringName(option: Option[String]): String = {
    option match {
      case Some(name) => name
      case None => "error"
    }
  }
}
