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

case class Channel(sink : Sink[Message, NotUsed], source: Source[Message, NotUsed])

@Singleton
class RoomClient @Inject()(implicit val materializer: Materializer, implicit val system: ActorSystem) {

  import RoomClient._

  def chatRoom(roomId: String): Room = synchronized {
    roomPool.get.get(roomId) match {
      case Some(room) =>
        room
      case None =>
        val room = create(roomId)
        roomPool.get() += (roomId -> room)
        room
    }
  }

  private def create(roomId: String): Room = {

    val (sink, source) =
      MergeHub.source[Message](perProducerBufferSize = 16)
          .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
          .run()

    source.runWith(Sink.ignore)

    val channel = Channel(sink, source)

    val bus: Flow[Message, Message, UniqueKillSwitch] = Flow.fromSinkAndSource(channel.sink, channel.source)
        .joinMat(KillSwitches.singleBidi[Message, Message])(Keep.right)
        .backpressureTimeout(3.seconds)

    Room(roomId, bus)
  }
}

object RoomClient {
  
//  private var rooms: MutableMap[String, Room] = MutableMap()

  val roomPool: AtomicReference[MutableMap[String, Room]] = new AtomicReference[MutableMap[String, Room]](MutableMap[String, Room]())

}