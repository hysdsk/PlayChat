package chat.actors

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import play.api.libs.functional.syntax._
import play.api.libs.json._
import chat._

case class ResForm(user: String, text: String)

class ResponseActor(out: ActorRef) extends Actor {
  
  implicit val locationWrites: Writes[ResForm] = (
    (JsPath \ "user").write[String] and
    (JsPath \ "text").write[String]
  )(unlift(ResForm.unapply))
  
  override def receive: Receive = {
    case Speech(u, t) =>
      out ! Json.toJson(ResForm(u, t))
  }

  override def postStop(): Unit = super.postStop()

}

object ResponseActor {
  def props(out: ActorRef): Props = Props(new ResponseActor(out))
}