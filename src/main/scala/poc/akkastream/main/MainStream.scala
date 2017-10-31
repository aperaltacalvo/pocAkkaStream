package poc.akkastream.main

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy, SourceShape}
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber, KafkaSubscriber}
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{Publisher, PublisherBase}
import poc.akkastream.{ActorProxy, AsyncMessageConsumer}
import poc.akkastream.kafka.{KafkaConsumer, KafkaProducer}

object MainStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()


  val flowFormat = Flow[String].map(_.filterNot(_.isDigit).toUpperCase)
  //s.split(":").filterNot(_.exists(_.isDigit)).mkString(" ")
  val flowIdentifier = Flow[String].map(message => {
    message match {
      case message if message contains("RABBIT") => message + " --- FROM RABBIT BROKER"
      case message if message contains("KAFKA") => message + " --- FROM KAFKA BROKER"
    }

  })

  /** Publishing in rabbit and kafka **/
 //publishInRabbit
 publishInKafka
  /** Publishing in rabbit and kafka **/

  //Define sources of akka stream
// val sourceRabbit = initSource(1)

     val sourceKafka = initSource(1)


    val sinkKafka = initSink(actorSink = Props[KafkaSubscriber])


  //Define sink of akka stream
  //Init streams
//  val sinkRabbit =  initSink(Props[CamelSubscriber])


  //val actorSourceRabbit = sourceRabbit.buffer(200,OverflowStrategy.backpressure) via flowFormat  via flowIdentifier to sinkRabbit run()


 //  val actorSourceKafka = sourceKafka.buffer(200,OverflowStrategy.backpressure) via flowFormat via flowIdentifier to sinkKafka run()

  //Init source actors
  //val asyncMessageActorRabbit = system.actorOf(Props(new AsyncMessageConsumer(actorSourceRabbit)))
  //val asyncMessageActorKafka = system.actorOf(Props(new AsyncMessageConsumer(actorSourceKafka)))

  //Init consumers from kafka and rabbit
  val kafkaConsumer = initConsumersFromBrokerKafka(system.actorOf( Props[KafkaSubscriber]) )
 //val camelConsumer = initConsumersFromBrokerRabbit(asyncMessageActorRabbit)



  private def publishInRabbit = {
    val publish: PublisherBase = Publisher.apply
    publish.basicPublish("192.168.16.172", 8081, "hola vengo de rabbit")("consumerExchange", "cola1", "camel", 5000)
  }

  private def publishInKafka = {
    val kafka: KafkaProducer = new KafkaProducer
    kafka.produce("Hola vengo de kafka")
  }

  private def initSink(actorSink: Props) = {
    Sink.actorRefWithAck[String](system.actorOf(actorSink), INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)
  }

  private def initSource(bufferSize: Int) = {
    Source.actorRef(bufferSize, OverflowStrategy.dropBuffer)
  }

  private def initConsumersFromBrokerKafka(actorRef: ActorRef): ActorRef = {
    val actor = system.actorOf(Props(new KafkaConsumer(actorRef)),"kafkaConsumer")
    actor.tell("", kafkaConsumer)
    actor
  }

  private def initConsumersFromBrokerRabbit(actorRef: ActorRef) = {
    system.actorOf(Props(new CamelConsumer(actorRef)),"camelConsumer")
  }
}


