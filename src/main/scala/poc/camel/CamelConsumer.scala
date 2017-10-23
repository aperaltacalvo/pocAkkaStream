package poc.camel

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

class CamelConsumer(actorRef: ActorRef) extends Consumer with ActorPublisher[String]{


  def endpointUri = "rabbitmq://127.0.0.1:8081/consumerExchange?username=guest&password=guest&autoDelete=false&routingKey=camel&queue=cola1"

  def receive = {
    case Cancel =>
      context.stop(self)
    case msg => actorRef ! msg.toString()
  }

}
