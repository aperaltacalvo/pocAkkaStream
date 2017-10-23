package poc.camel

import akka.actor.{Actor, ActorRef}
import akka.camel.{CamelMessage, Consumer}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

class CamelConsumer extends Consumer with ActorPublisher[String] {
  def endpointUri = "rabbitmq://127.0.0.1:8081/consumerExchange?username=guest&password=guest&autoDelete=false&routingKey=camel&queue=cola1"
  val actor=new AsyncMessageConsumer
  def receive = {
    case Request(_) => //ignored
    case Cancel =>
      context.stop(self)
    case msg =>  actor.sender().tell(msg.toString,actor.self)
  }
}
