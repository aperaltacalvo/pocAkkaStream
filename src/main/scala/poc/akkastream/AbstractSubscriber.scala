package poc.akkastream

import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy}
abstract class AbstractSubscriber extends ActorSubscriber{


    override val requestStrategy = OneByOneRequestStrategy



}
