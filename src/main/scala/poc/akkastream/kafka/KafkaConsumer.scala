package poc.akkastream.kafka

import akka.Done
import akka.actor.{ActorRef, Props}
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.remote.WireFormats.FiniteDuration
import akka.stream.{OverflowStrategy, ThrottleMode}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Sink
import poc.akkastream.camel.KafkaSubscriber
import poc.akkastream.protocol.{ACK, INITMESSAGE, ONCOMPLETE}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration



class KafkaConsumer(actorRef:ActorRef) extends KafkaConn with ActorPublisher[String]{
 var n = 1;

  def startConsume = {

    Consumer.plainSource(consumerSettings, Subscriptions.topics("topic1")).map(msg => {
      println(s"event $msg")
    })
      //.to(Sink.ignore).run
       // .runForeach(msg  =>  actorRef ! msg.value())
            //.runForeach(msg => actorRef ! msg.value())
              .to(Sink.actorRefWithAck(actorRef,INITMESSAGE,ACK,ONCOMPLETE,th => th.getMessage)).run()

  }


  override def receive = {
    case msg:String =>
      startConsume

  }
}
