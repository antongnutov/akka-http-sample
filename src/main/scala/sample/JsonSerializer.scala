package sample

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

import scala.util.Try

/**
  * @author Anton Gnutov
  */
trait JsonSerializer {
  implicit val formats = Serialization.formats(NoTypeHints)

  def decodeClusterState(request: String) = Try {
    read[ClusterState](request)
  }
}
