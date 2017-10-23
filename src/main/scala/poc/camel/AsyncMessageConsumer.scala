package poc.camel

import akka.actor.ActorRef
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import poc.camel.protocol.ACK

class AsyncMessageConsumer(actorSource:ActorRef) extends ActorPublisher[String] {



  override def receive = {
    case msg:String => actorSource ! msg.toString
    case Cancel =>
      context.stop(self)

  }
}
