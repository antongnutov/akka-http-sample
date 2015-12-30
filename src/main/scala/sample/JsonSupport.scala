package sample

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * @author Anton Gnutov
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val addressFormat = jsonFormat4(Address)
  implicit val uniqueAddressFormat = jsonFormat2(UniqueAddress)
  implicit val memberFormat = jsonFormat4(Member)
  implicit val clusterStateFormat = jsonFormat2(ClusterState)
}
