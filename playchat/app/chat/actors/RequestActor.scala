package chat.actors

import akka.actor._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import chat._

class RequestActor(out: ActorRef) extends Actor {
  
  override def receive: Receive = {
    case msg: JsValue => {
      val user = (msg \ "user").as[String]
      val text = (msg \ "text").as[String]
      out ! Speech(user, text)
    }
  }

  override def preStart() = println(s"Start ${out.path}")

  override def postStop() = println(s"Stop ${out.path}")
  
}

object RequestActor {
  def props(out: ActorRef): Props = Props(new RequestActor(out))
}