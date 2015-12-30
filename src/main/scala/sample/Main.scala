package sample

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import sample.CheckHttpActor.{CheckRequest, CheckResponse, GracefulStop}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {
  val log = LoggerFactory.getLogger(Main.getClass)

  val config = ConfigFactory.load()
  val uri = config.getString("sample.uri")

  val system = ActorSystem("sample")

  implicit val timeout = Timeout(10.seconds)
  implicit val executor = ExecutionContext.global

  val response = (system.actorOf(CheckHttpActor.props, "checkHttp") ? CheckRequest(uri)).mapTo[CheckResponse].onComplete {
    case Success(value) =>
      log.info("Received response code: {}", value.code)
      stopAll()
    case Failure(e) =>
      log.warn("Http request failed: {}", e.getMessage)
      stopAll()
  }

  def stopAll(): Unit = {
    (system.actorSelection("/user/checkHttp") ? GracefulStop).onComplete {
      case Success(_) =>
        log.info("Stopped successfully")
        system.terminate()
      case Failure(e) =>
        log.warn("Http stop failure: {}", e.getMessage)
        system.terminate()
    }
  }
}