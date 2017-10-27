package poc.akkastream.kafka

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.remote.WireFormats.FiniteDuration
import akka.stream.ThrottleMode
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration


class KafkaConsumer(actorRef: ActorRef) extends KafkaConn with ActorPublisher[String] {
  def consume = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics("topic1"))
      .map(doSomething)
      .throttle(
      elements = 1, //number of elements to be taken from bucket
      per = duration.FiniteDuration.apply(100, TimeUnit.SECONDS),
      maximumBurst = 1, //capacity of bucket
      mode = ThrottleMode.shaping
    )
      .runWith(Sink.ignore)
  }

  def doSomething(record: ConsumerRecord[Array[Byte], String]): Unit = {
    actorRef ! record.value().toString
  }

  override def receive = {
    case msg: String => consume
  }
}
