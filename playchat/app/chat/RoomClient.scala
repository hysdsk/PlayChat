package chat

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Sink }
import akka.stream.{ KillSwitches, Materializer, UniqueKillSwitch }
import akka.stream.scaladsl.{ Sink, Source }
import akka.NotUsed

import scala.collection.mutable.{ Map => MutableMap }
import scala.concurrent.duration._

case class Room(roomId: String, bus: Flow[Message, Message, UniqueKillSwitch])

@Singleton
class RoomClient @Inject()(implicit val materializer: Materializer, implicit val system: ActorSystem) {

  def chatRoom(roomId: String): Room = synchronized {
    RoomClient.roomPool.get.get(roomId) match {
      case Some(room) =>
        room
      case None =>
        val room = create(roomId)
        RoomClient.roomPool.get() += (roomId -> room)
        room
    }
  }

  private def create(roomId: String): Room = {

    val (sink, source) =
      MergeHub.source[Message](perProducerBufferSize = 16)
          .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
          .run()
          
    source.runWith(Sink.ignore)

    val bus = Flow.fromSinkAndSource(sink, source)
        .joinMat(KillSwitches.singleBidi[Message, Message])(Keep.right)
        .backpressureTimeout(3.seconds)

    Room(roomId, bus)
  }
}

object RoomClient {
  
  val roomPool = new AtomicReference[MutableMap[String, Room]](MutableMap[String, Room]())

}