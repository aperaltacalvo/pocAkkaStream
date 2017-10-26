package poc.akkastream

import akka.actor.ActorRef
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Cancel
import poc.akkastream.protocol.ACK

class AsyncMessageConsumer(actorRef:ActorRef) extends ActorPublisher[String] {

  override def receive = {
    case msg:String => actorRef ! msg
    case Cancel =>
      context.stop(self)

  }
}
