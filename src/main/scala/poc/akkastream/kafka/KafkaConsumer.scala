package poc.akkastream.kafka

import akka.Done
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object KafkaConsumer extends App {
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
    println(s"Consumed: ${record.value}")
    Future.successful(Done)
  }
}
