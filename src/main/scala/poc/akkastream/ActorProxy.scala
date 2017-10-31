package poc.akkastream

import akka.actor.ActorRef
import akka.stream.actor.ActorPublisher
import poc.akkastream.protocol.ACK

class ActorProxy(actorRef: ActorRef) extends ActorPublisher[String]{
  override def receive = {
    case msg => actorRef ! msg
  }
}
