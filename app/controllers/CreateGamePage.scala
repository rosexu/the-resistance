package controllers

import play.api.mvc.{Action, Controller}

/**
 * Created by rosexu on 15-08-29.
 * @author Rose Xu
 */
class CreateGamePage extends Controller {
  def index = Action {
    Ok(views.html.creategame())
  }
}
