package poc.camel.main

import akka.actor.{ActorSystem, Props}
import akka.camel.CamelMessage
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import poc.camel.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.camel.publisher.{Publisher, PublisherBase}
import poc.camel.{AsyncMessageConsumer, CamelConsumer, CamelSubscriber}

object MainStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  val source = Source.actorRef(50000,OverflowStrategy.fail)
  val sink = Sink.actorRefWithAck[String](system.actorOf(Props[CamelSubscriber]),INITMESSAGE,ACK,ONCOMPLETE, th => th.getMessage)
  val flowFormat = Flow[String].map(s => {
    s.split(":").filterNot(_.exists(_.isDigit)).mkString(" ")
  })
  val flowIdentifier = Flow[String].filter(c => c.contains("pepe")).map(s => s.replace("pepe", "Sr. Pepe"))

  val publish: PublisherBase = Publisher.apply
  publish.basicPublish("192.168.16.172", 8081, "hola:soy:pepe", "consumerExchange", "cola1", "camel", 5000)


  val actorSource =  source via flowFormat via flowIdentifier to sink run()

  val asyncMessageActor = system.actorOf(Props(new AsyncMessageConsumer(actorSource)))
  val camelConsumer = system.actorOf(Props(new CamelConsumer(actorRef = asyncMessageActor)))

}
