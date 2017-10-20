package sample.stream

import akka.NotUsed
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage
import akka.stream.actor.OneByOneRequestStrategy
import akka.actor.{ActorSystem, Props}
import akka.camel.{CamelMessage, Consumer, Producer}
import akka.io.Udp.SO.Broadcast
import akka.remote.ContainerFormats.ActorRef
import akka.remote.WireFormats.FiniteDuration
import akka.stream.actor.ActorPublisher
import com.rabbitmq.client.MessageProperties
import com.sun.corba.se.impl.orbutil.graph.Graph
import sun.jvm.hotspot.runtime.Bytes

import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait ProtocolBackPressureACK

case object ACK extends ProtocolBackPressureACK
case object INITMESSAGE extends ProtocolBackPressureACK
case object ONCOMPLETE extends ProtocolBackPressureACK

object CamelDriverACK extends App {


  class CamelConsumer extends Consumer with ActorPublisher[String] {
    def endpointUri = "rabbitmq://127.0.0.1:8081/consumerExchange?username=guest&password=guest&autoDelete=false&routingKey=camel&queue=cola1"

    import akka.stream.actor.ActorPublisherMessage._

    def receive = {
      /*   case msg: CamelMessage =>
           msg.bodyAs[String] match {
             case "stop" => onComplete()
             case msg if totalDemand > 0 =>
               onNext(msg)
           }*/
      case Request(_) => //ignored
      case Cancel =>
        context.stop(self)
      case msg if totalDemand > 0 => onNext(msg.toString)
    }
  }


  class CamelSubscriber extends ActorSubscriber {
    import ActorSubscriberMessage._

    override val requestStrategy = OneByOneRequestStrategy


    def receive = {
      /*    case OnComplete             => system.terminate()
          case OnNext(string: String) => println(string)*/
      case msg:String => println(msg)
        sender ! ACK
      case INITMESSAGE => println(s"initMessage")
        sender ! ACK
    }
  }

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()


  val source = Source.actorPublisher[String](Props[CamelConsumer])
  val sink = Sink.actorRefWithAck[String](system.actorOf(Props[CamelSubscriber]),INITMESSAGE,ACK,ONCOMPLETE, th => th.getMessage)
  //val sink = Sink.actorSubscriber[String](Props[CamelSubscriber])

  import com.rabbitmq.client.ConnectionFactory

  val factory = new ConnectionFactory
  factory.setHost("127.0.0.1")
  factory.setPort(8081)
  val connection = factory.newConnection
  val channel = connection.createChannel

  var message = "hola"
  val exchange=channel.exchangeDeclare("consumerExchange","direct",true)
  val queue=channel.queueDeclare("cola1",true,false,false, null)
  channel.queueBind("cola1","consumerExchange","camel")

  for(i<-0 to 5000) {
    message = message + i
    channel.basicPublish("consumerExchange", "camel", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())
  }


  source
    .map(input => input.toUpperCase)
    // .throttle(elements = 1, per= 1.second, maximumBurst = 1, ThrottleMode.shaping)
    .to(sink)
    .run()
}