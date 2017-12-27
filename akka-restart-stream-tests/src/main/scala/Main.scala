import akka.stream.scaladsl.{ RestartSource, Sink, Source }
import akka.stream.{ ActorMaterializer, Attributes, Inlet, SinkShape }
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler }
import akka.actor.ActorSystem

import scala.concurrent.duration._

object Consumer {
  def apply[T] = new Consumer[T]
}

class Consumer[T] extends GraphStage[SinkShape[T]] {
  val in = Inlet[T]("Consumer.in")
  override def shape = SinkShape(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler {
      override def preStart(): Unit = {
        pull(in)
      }

      override def onPush(): Unit = {
        println(s"we get ${grab(in)}")
        pull(in)
      }

      setHandler(in, this)
    }
}

object Main {
  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val graph = RestartSource.withBackoff(10.millis, 20.millis, 0) { () ⇒
      Source(List("a", "b"))
    }.runWith(Sink.fromGraph(Consumer[String]))

    system.terminate()
  }
}
