package sample

/**
  * @author Anton Gnutov
  */
case class Address(protocol: String, system: String, host: Option[String], port: Option[Int])
case class UniqueAddress(address: Address, uid: Int)
case class Member(uniqueAddress: UniqueAddress, upNumber: Int, status: String, roles: Set[String])
case class ClusterState(members: Set[Member], leader: Option[Address])
