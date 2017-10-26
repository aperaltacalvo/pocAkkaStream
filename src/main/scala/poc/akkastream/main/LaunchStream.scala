package poc.akkastream.main

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
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
  val consumerActor = system.actorOf(Props[CamelConsumer])

  val source: Source[String, NotUsed] = {
    val publisher: Publisher[String] = ActorPublisher(consumerActor)
    Source.fromPublisher(publisher).buffer(5000, OverflowStrategy.backpressure)
  }
  val sink =
    Sink.actorRefWithAck[String](system.actorOf(Props(new CamelSubscriber(consumerActor.path))), INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)

  publishInRabbit

  val graphScenario = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = source
    val out = sink

    val f1 = Flow[String].map(_.toString)
    val f2 = Flow[String].map(_ + " Flow2")

    in ~> f1 ~> f2 ~> out
    ClosedShape
  }).run()


  private def publishInRabbit = {
    val publish: PublisherBase = PublisherMain.apply
    publish.basicPublish("localhost", 8081, "hola vengo de rabbit")("consumerExchange", "cola1", "camel", 50000)
  }
}
