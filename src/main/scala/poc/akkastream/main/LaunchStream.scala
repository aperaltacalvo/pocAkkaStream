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

  publishInRabbit

  //Scenario with 1000 buffered
  val camelConsumerActor = consumerCamelActor
  graphCamelScenario(sourceForCamel(camelConsumerActor), sinkForCamel(camelConsumerActor), 1000).run()


  /** CAMEL AKKA STREAMING **/

  private def graphCamelScenario(source: Source[String, NotUsed], sink: Sink[String, NotUsed], buffer: Int) =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = source.buffer(buffer, OverflowStrategy.backpressure)
      val out = sink

      val f1 = Flow[String].map(_.toString)
      val f2 = Flow[String].map(_ + " Flow2")

      in ~> f1 ~> f2 ~> out
      ClosedShape
    })


  private def publishInRabbit = {
    val publish: PublisherBase = PublisherMain.apply
    publish.basicPublish("localhost", 8081, "hola vengo de rabbit")("consumerExchange", "cola1", "camel", 10000)
  }

  private def consumerCamelActor = system.actorOf(Props[CamelConsumer])

  private def sourceForCamel(consumer: ActorRef): Source[String, NotUsed] = {
    val publisher: Publisher[String] = ActorPublisher(consumer)
    Source.fromPublisher(publisher)
  }

  private def sinkForCamel(consumer: ActorRef) =
    Sink.actorRefWithAck[String](system.actorOf(Props(new CamelSubscriber(consumer.path))),
      INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)
}
