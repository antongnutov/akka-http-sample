package sample

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.ByteString
import sample.CheckHttpActor.{CheckRequest, CheckResponse, GracefulStop}
import spray.json._

import scala.util.{Try, Failure, Success}

/**
  * @author Anton Gnutov
  */
class CheckHttpActor extends Actor with ImplicitMaterializer with JsonSupport with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  val http = Http(context.system)

  override def receive = {
    case CheckRequest(uri) =>
      log.debug("Checking uri: {} ...", uri)
      val replyTo = sender()
      http.singleRequest(HttpRequest(uri = uri)).pipeTo(self)(replyTo)

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      val replyTo = sender()
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).pipeTo(self)(replyTo)

    case HttpResponse(code, _, _, _) =>
      log.warning("Request failed, response code: {}", code)
      sender() ! CheckResponse(code)

    case bs: ByteString =>
      val string: String = bs.decodeString("UTF-8")
      log.info("Received response: {}", string)

      Try(string.parseJson.convertTo[ClusterState]) match {
        case Success(state) => log.info("Cluster state: {}", state)
        case Failure(e) => log.warning("Could not deserialize cluster state: {}", e.getMessage)
      }

      sender() ! CheckResponse(StatusCodes.OK)

    case GracefulStop => http.shutdownAllConnectionPools().pipeTo(sender())
  }
}

object CheckHttpActor {
  def props: Props = Props(classOf[CheckHttpActor])

  case class CheckRequest(uri: String)
  case class CheckResponse(code: StatusCode)
  case object GracefulStop
}

