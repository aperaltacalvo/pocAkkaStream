package poc.camel

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

class AsyncMessageConsumer extends ActorPublisher[String] {



  override def receive = {
    case Request(_) => //ignored
    case Cancel =>
      context.stop(self)
    case msg if totalDemand > 0 => onNext(msg.toString)
  }
}
