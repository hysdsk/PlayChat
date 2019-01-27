package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Source }
import javax.inject._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.libs.json.JsValue

import chat._
import chat.actors._
import play.api.libs.iteratee.Concurrent

@Singleton
class ChatController @Inject() (implicit system: ActorSystem, materializer: Materializer, roomClient: RoomClient) extends Controller {
  
  def get = Action { implicit request =>
    Ok(views.html.chat())
  }
  
  def ws(roomId: String) = WebSocket.accept[JsValue, JsValue] { request =>
    
    val room = roomClient.chatRoom(roomId)
    
    val userInput = ActorFlow.actorRef[JsValue, Message](RequestActor.props)
    
    val userOutPut = ActorFlow.actorRef[Message, JsValue](ResponseActor.props)
    
    userInput.viaMat(room.bus)(Keep.right).viaMat(userOutPut)(Keep.right)

  }
  
}