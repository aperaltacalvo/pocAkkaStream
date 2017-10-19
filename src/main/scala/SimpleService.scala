package sample.stream

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage
import akka.stream.actor.OneByOneRequestStrategy
import akka.actor.{ActorSystem, Props}
import akka.camel.{CamelMessage, Consumer, Producer}
import akka.stream.actor.ActorPublisher

object CamelDriver extends App {


  class CamelConsumer extends Consumer with ActorPublisher[String] {
    def endpointUri = "rabbitmq://127.0.0.1:8081/consumerExchange?username=guest&password=guest&queue=cola1&declare=true"

    import akka.stream.actor.ActorPublisherMessage._

    def receive = {
      case msg: CamelMessage =>
        msg.bodyAs[String] match {
          case "stop" => onComplete()
          case string if (totalDemand > 0) =>
            onNext(string)
        }
      case Request(_) => //ignored
      case Cancel =>
        context.stop(self)
    }
  }

  class CamelProducer extends Producer {
    def endpointUri = "rabbitmq://127.0.0.1:8081/consumerExchange?username=guest&password=guest&queue=cola2&declare=true"
  }

  class CamelSubscriber extends ActorSubscriber {
    import ActorSubscriberMessage._

    override val requestStrategy = OneByOneRequestStrategy

    val endPoint = context.actorOf(Props[CamelProducer])

    def receive = {
      case OnComplete             => system.terminate()
      case OnNext(string: String) => endPoint ! string
    }
  }

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  val source = Source.actorPublisher[String](Props[CamelConsumer])
  val sink = Sink.actorSubscriber[String](Props[CamelSubscriber])

  source.map(_.toUpperCase).
    to(sink).
    run()
}