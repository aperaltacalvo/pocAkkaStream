package poc.akkastream.main

import akka.NotUsed
import akka.actor.{ActorRef, Props}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import org.reactivestreams.Publisher
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber}
import poc.akkastream.kafka.KafkaConsumer
import poc.akkastream.main.LaunchStream.system
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{PublisherBase, PublisherKafkaMain}

object AkkaStreamKafka{
  def apply: AkkaStreamKafka = new AkkaStreamKafka()
}

class AkkaStreamKafka {

  def graphNormalKafkaScenario(source: Source[String, NotUsed], sink: Sink[String, NotUsed], buffer: Int) =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = source.buffer(buffer, OverflowStrategy.backpressure)
      val out = sink

      val f1 = Flow[String].map(_.toString)
      val f2 = Flow[String].map(_ + " Flow2")

      in ~> f1 ~> f2 ~> out
      ClosedShape
    })

  def publishInKafka = {
    val publish: PublisherBase = PublisherKafkaMain.apply
    publish.basicPublish("localhost", 9092, "hola vengo de kafka", 1000)("","","","topic1")
  }

  def consumerKafkaActor = system.actorOf(Props[KafkaConsumer])

  def sourceForKafka(consumer: ActorRef): Source[String, NotUsed] = {
    val publisher: Publisher[String] = ActorPublisher(consumer)
    Source.fromPublisher(publisher)
  }

  def sinkForKafka(consumer: ActorRef) =
    Sink.actorRefWithAck[String](system.actorOf(Props(new CamelSubscriber(consumer.path))),
      INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)
}
