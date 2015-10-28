package controllers

import play.api.mvc.{Action, Controller}

object AS4Controller extends Controller {
  def as4GetEndpoint() = Action{ request =>
    Ok("<h3>This the AS4 endpoint, send SOAP messages please!</h3>")
  }

  def as4PostEndpoint() = Action(parse.raw){ request =>
    val stream = request.body

    val theMessage: String = new Predef.String(stream.asBytes().get)
    println(theMessage)

    Ok(theMessage)
  }

  def c2() = Action(parse.raw){ request =>
    println("C2")
    val stream = request.body

    val theMessage: String = new Predef.String(stream.asBytes().get)
    println(theMessage)

    Ok(theMessage)
  }

  def c3()  = Action(parse.raw){ request =>
    println("C3")
    val stream = request.body

    val theMessage: String = new Predef.String(stream.asBytes().get)
    println(theMessage)

    Ok(theMessage)
  }
}
