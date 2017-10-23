package poc.camel.main

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import poc.camel.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.camel.publisher.{Publisher, PublisherBase}
import poc.camel.{AsyncMessageConsumer, CamelConsumer, CamelSubscriber}

object MainStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  //val source = Source.actorPublisher[String](Props[AsyncMessageConsumer])

  val source = Source.actorRef(50000,OverflowStrategy.fail)


  val sink = Sink.actorRefWithAck[String](system.actorOf(Props[CamelSubscriber]),INITMESSAGE,ACK,ONCOMPLETE, th => th.getMessage)

  val publish: PublisherBase = Publisher.apply
  publish.basicPublish("127.0.0.1", 8081, "hola", "consumerExchange", "cola1", "camel", 5000)


  val actorSource =  source
    .to(sink)
    .run()
  val asyncMessageActor = system.actorOf(Props(new AsyncMessageConsumer(actorSource)))

  val camelConsumer = system.actorOf(Props(new CamelConsumer(actorRef = asyncMessageActor)))

}
