package poc.akkastream.main

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber}
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{Publisher, PublisherBase}

object LaunchStream {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  val source = Source.actorPublisher[String](Props[CamelConsumer])
  val sink = Sink.actorRefWithAck[String](system.actorOf(Props[CamelSubscriber]), INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)

  publishInRabbit

  source
    .map(input => input.toUpperCase)
    .to(sink)
    .run()


  private def publishInRabbit = {
    val publish: PublisherBase = Publisher.apply
    publish.basicPublish("localhost", 8081, "hola vengo de rabbit")("consumerExchange", "cola1", "camel", 5000)
  }
}
