package poc.akkastream.main

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import poc.akkastream.camel.{CamelConsumer, CamelSubscriber}
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}
import poc.akkastream.publisher.{Publisher, PublisherBase}
import poc.akkastream.AsyncMessageConsumer
import poc.akkastream.kafka.{KafkaConsumer, KafkaProducer}

object MainStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  //Define sources of akka stream
  val sourceRabbit = initSource(bufferSize = 5000)
  val sourceKafka = initSource(bufferSize = 5000)


  //Define sink of akka stream
  val sinkRabbit = initSink(actorSink=Props[CamelSubscriber])
  val sinkKafka = initSink(actorSink=Props[CamelSubscriber])

  val flowFormat = Flow[String].map(s => s.toString)
    //s.split(":").filterNot(_.exists(_.isDigit)).mkString(" ")
  val flowIdentifier = Flow[String].filter(c => c.contains("pepe")).map(s => s.replace("pepe", "Sr. Pepe"))

  /** Publishing in rabbit and kafka**/
  publishInRabbit
  publishInKafka
  /** Publishing in rabbit and kafka**/


    //Init streams
  val actorSourceRabbit =  sourceRabbit /*via flowFormat via flowIdentifier*/ to sinkRabbit run()
  val actorSourceKafka =  sourceKafka /*via flowFormat via flowIdentifier*/ to sinkKafka run()

  //Init source actors
  val asyncMessageActorRabbit = system.actorOf(Props(new AsyncMessageConsumer(actorSourceRabbit)))
  val asyncMessageActorKafka = system.actorOf(Props(new AsyncMessageConsumer(actorSourceKafka)))


  //Init consumers from kafka and rabbit
  val kafkaConsumer = initConsumersFromBrokerKafka(asyncMessageActorKafka)
  val camelConsumer = initConsumersFromBrokerRabbit(asyncMessageActorRabbit)



  private def publishInRabbit = {
    val publish: PublisherBase = Publisher.apply
    publish.basicPublish("localhost", 8081, "hola:soy:pepe")("consumerExchange", "cola1", "camel", 5000)
  }

  private def publishInKafka = {
    val kafka:KafkaProducer = new KafkaProducer
    kafka.produce
  }

  private def initSink(actorSink:Props) = {
    Sink.actorRefWithAck[String](system.actorOf(actorSink), INITMESSAGE, ACK, ONCOMPLETE, th => th.getMessage)
  }

  private def initSource(bufferSize:Int) = {
    Source.actorRef(bufferSize, OverflowStrategy.fail)
  }

  private def initConsumersFromBrokerKafka(actorRef: ActorRef):ActorRef = {
   val actor = system.actorOf(Props(new KafkaConsumer(actorRef)))
    actor.tell("",kafkaConsumer)
    actor
  }
  private def initConsumersFromBrokerRabbit(actorRef: ActorRef) = {
    system.actorOf(Props(new CamelConsumer(actorRef)))
  }
}


