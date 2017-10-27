package poc.akkastream.main

import akka.NotUsed
import akka.actor.{ActorRef, Props}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import org.reactivestreams.Publisher
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber}
import poc.akkastream.main.LaunchStream.system
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{PublisherBase, PublisherRabbitMain}

object AkkaStreamCamel{
  def apply: AkkaStreamCamel = new AkkaStreamCamel()
}

class AkkaStreamCamel {

  def graphNormalCamelScenario(source: Source[String, NotUsed], sink: Sink[String, NotUsed], buffer: Int) =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = source.buffer(buffer, OverflowStrategy.backpressure)
      val out = sink

      val f1 = Flow[String].map(_.toString)
      val f2 = Flow[String].map(_ + " Flow2")

      in ~> f1 ~> f2 ~> out
      ClosedShape
    })


  def publishInRabbit = {
    val publish: PublisherBase = PublisherRabbitMain.apply
    publish.basicPublish("localhost", 8081, "hola vengo de rabbit", 10000)("consumerExchange", "cola1", "camel","")
  }

  def consumerCamelActor = system.actorOf(Props(new CamelConsumer),"camelConsumer")

  def sourceForCamel(consumer: ActorRef): Source[String, NotUsed] = {
    val publisher: Publisher[String] = ActorPublisher(consumer)
    Source.fromPublisher(publisher)
  }

  def sinkForCamel =
    Sink.actorRefWithAck[String](system.actorOf(Props(new CamelSubscriber)),
      INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)
}
