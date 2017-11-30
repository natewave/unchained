import akka.stream.ActorMaterializer

object Commons {
  lazy val actorSys = akka.actor.ActorSystem("Tests")

  implicit lazy val materializer = ActorMaterializer()(actorSys)
}
