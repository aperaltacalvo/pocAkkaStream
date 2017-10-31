package poc.akkastream.camel

import akka.stream.actor.{OneByOneRequestStrategy}
import poc.akkastream.AbstractSubscriber
import poc.akkastream.protocol.{ACK, INITMESSAGE}

class CamelSubscriber extends AbstractSubscriber {

  override def receive = {
    case msg:String =>
     println("received %s" format msg)
      sender ! ACK
      context.actorSelection("akka://some-system/user/camelConsumer") ! ACK
    case INITMESSAGE =>
      println(s"initMessage")
      sender ! ACK
    case msg =>
      println(s"Untyped Message $msg")
      sender ! ACK
  }
}
