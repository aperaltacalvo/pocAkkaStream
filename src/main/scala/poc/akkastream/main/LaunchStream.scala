package poc.akkastream.main

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

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
    akkaStream.graphNormalCamelScenario(akkaStream.sourceForCamel(camelConsumerActor), akkaStream.sinkForCamel(camelConsumerActor), 5000).run()

  }

  def callKafkaProcess = {
    val kafkaStream: AkkaStreamKafka = AkkaStreamKafka.apply
    kafkaStream.publishInKafka

    //Scenario with 1000 buffered
    val kafkaConsumerActor = kafkaStream.consumerKafkaActor
    kafkaStream.graphNormalKafkaScenario(kafkaStream.sourceForKafka(kafkaConsumerActor), kafkaStream.sinkForKafka(kafkaConsumerActor), 5000).run()
  }


}
