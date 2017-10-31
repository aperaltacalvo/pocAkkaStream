package poc.akkastream.camel

import poc.akkastream.AbstractSubscriber
import poc.akkastream.protocol.{ACK, INITMESSAGE, MANUAL}

class KafkaSubscriber extends AbstractSubscriber {

  override def receive = {
    case msg:String =>
      println("received %s" format msg)
      sender ! ACK
      //context.actorSelection("akka://some-system/user/kafkaConsumer") ! ACK
    case INITMESSAGE =>
      println(s"initMessage")
      sender ! ACK
    case MANUAL =>
    case msg =>
      println("Untyped Message %s" format msg)
      sender ! ACK
  }
}
