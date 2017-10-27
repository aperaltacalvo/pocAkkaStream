package poc.akkastream.main

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import poc.akkastream.camel.CamelSubscriber

object LaunchStream extends App {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  //callCamelRabbitProcess
  callKafkaProcess

  def callCamelRabbitProcess = {

    val akkaStream: AkkaStreamCamel = AkkaStreamCamel.apply
    akkaStream.publishInRabbit

    //Scenario with 1000 buffered
    val camelConsumerActor = akkaStream.consumerCamelActor
    akkaStream.graphNormalCamelScenario(akkaStream.sourceForCamel(camelConsumerActor), akkaStream.sinkForCamel, 5000).run()

  }

  def callKafkaProcess = {
    val kafkaStream: AkkaStreamKafka = AkkaStreamKafka.apply
    //kafkaStream.publishInKafka

    //Scenario with 1000 buffered
    val actor = kafkaStream.sourceForKafka via kafkaStream.f1 via kafkaStream.f2 to kafkaStream.sinkForKafka(Props[CamelSubscriber]) run()
    val kafkaConsumerActor = kafkaStream.consumerKafkaActor(actor)
    kafkaConsumerActor ! "WAKE UP KAFKA CONSUMER"
  }


}
