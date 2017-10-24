package poc.akkastream.kafka

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.util.{Failure, Success}

object KafkaProducer extends App {
  val k: KafkaProducer = new KafkaProducer
  k produce
}

class KafkaProducer {
  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("172.17.0.4:9092")

  def produce = {
    val list = List.range(1, 500).map(l => "Kafka - " + l)
    val kafkaProducer = producerSettings.createKafkaProducer()
    val done = Source(list)
      .map(_.concat(". fuck yeah!"))
      .map { elem =>
        new ProducerRecord[Array[Byte], String]("topic1", elem)
      }
      .runWith(Producer.plainSink(producerSettings,kafkaProducer))

    done.onComplete {
      case Success(value) => println(s"Got the callback, meaning = $value")
      case Failure(e) => e.printStackTrace
    }
  }

}
