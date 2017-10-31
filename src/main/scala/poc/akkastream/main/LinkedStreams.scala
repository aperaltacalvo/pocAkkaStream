import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object LinkedStreams extends App{

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()

  /* Pieza externa que nos la suda */
  var flowExterno = Flow[Int].map(a => a.toString + " estoy en el externo")
  var sourceExterno = Source(List(1,2,3,4,5,6,7,8,9,10)).via(flowExterno)


 /* Pieza del core genérica que nos la pone tiesa */

  var flowCore = Flow[String].map(a => a + " estoy en el core")
  var sinkCore = Sink.ignore
  var sourceCore = Source(List("a", "b", "c","d", "e"))


  sourceExterno.concat(sourceCore).map(e => e.toString + " mierda puta desde el stream principal").runForeach(a => println(a))





}