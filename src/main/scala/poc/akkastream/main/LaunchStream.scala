package poc.akkastream.main

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object LaunchStream {

  implicit val system = ActorSystem("some-system")
  implicit val materializer = ActorMaterializer()


}
