package poc.akkastream.camel

import akka.actor.ActorRef
import akka.camel.{CamelMessage, Consumer}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Cancel
import poc.akkastream.protocol.ACK
import akka.camel.Ack

class CamelConsumer(actorRef: ActorRef) extends Consumer with ActorPublisher[String]{

  def endpointUri = "rabbitmq://192.168.16.172:8081/consumerExchange?username=guest&password=guest&autoDelete=false&routingKey=camel&queue=cola1"
  override def autoAck =false
  var rabbit:String = ""
  def receive = {
    case Cancel => context.stop(self)
    case msg: CamelMessage => rabbit=sender.path.toString
      actorRef ! msg.bodyAs[String]
    case ACK => println(ACK)
       context.actorSelection(rabbit) ! Ack

  }

}
