import akka.stream.scaladsl.{ RestartSource, Source, Sink }
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem

import scala.concurrent.duration._

object Main {
  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val graph = RestartSource.withBackoff(10.millis, 20.millis, 0) { () ⇒
      Source(List("a", "b"))
    }.runWith(Sink.foreach(println))

    system.terminate()
  }
}
