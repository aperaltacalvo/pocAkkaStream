package poc.akkastream.kafka

import akka.Done
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object KafkaConsumer{
  val k: KafkaConsumer = new KafkaConsumer
  k.consume
}

class KafkaConsumer extends KafkaConn {

  def consume = {
    val done =
      Consumer.plainSource(consumerSettings, Subscriptions.topics("topic1"))
        .mapAsync(1)(save)
        .runWith(Sink.ignore)

    done.onComplete {
      case Success(value) => println(s"Got the callback, meaning = $value")
      case Failure(e) => e.printStackTrace
    }
  }

  def save(record: ConsumerRecord[Array[Byte], String]): Future[Done] = {
    println(s"DB.save: ${record.value}")
    Future.successful(Done)
  }
}
