package poc.akkastream.main

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import org.reactivestreams.Publisher
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber}
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{PublisherBase, PublisherMain}

object LaunchStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  callCamelRabbitProcess

  def callCamelRabbitProcess = {

    val akkaStream: AkkaStreamCamel = AkkaStreamCamel.apply

    akkaStream.publishInRabbit

    //Scenario with 1000 buffered
    val camelConsumerActor = akkaStream.consumerCamelActor
    akkaStream.graphCamelScenario(akkaStream.sourceForCamel(camelConsumerActor), akkaStream.sinkForCamel(camelConsumerActor), 5000).run()

  }


}
