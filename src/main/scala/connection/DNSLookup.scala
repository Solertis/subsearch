package connection

import org.xbill.DNS._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.util.Try

class DNSLookup(private val hostname: String, private val resolvers: List[String]) {
  import DNSLookup.Record

  private val resolver: ExtendedResolver = new ExtendedResolver(resolvers.toArray)

  def hostIsValid(): Future[Boolean] =
    queryANY().map(_.nonEmpty)

  def queryANY(): Future[List[Record]] = queryANY(hostname)
  private def queryANY(hostname: String): Future[List[Record]] = queryType(Type.ANY, hostname)

  def queryNS(): Future[List[String]] = queryNS(hostname)
  private def queryNS(hostname: String): Future[List[String]] = queryType(Type.NS, hostname).map(recordsToData)

  def queryA(): Future[List[String]] = queryA(hostname)
  private def queryA(hostname: String): Future[List[String]] = queryType(Type.A, hostname).map(recordsToData)

  private def queryType(recordType: Int, name: String): Future[List[Record]] = {
    Future {
      val lookup = new Lookup(name, recordType)
      lookup.setResolver(resolver)

      Option(lookup.run())
        .getOrElse(Array.empty)
        .toList
        .map(dnsRecord => Record(Type.string(dnsRecord.getType), dnsRecord.rdataToString()))
    }
  }

  private def recordsToData(records: List[Record]): List[String] = records.map(_.data)

  def zoneTransfer(): Future[List[Record]] = {
    if (resolvers.size != 1)
      throw new IllegalArgumentException("Exactly one resolver must be supplied for a zone transfer.")

    Future {
      val transfer: ZoneTransferIn = ZoneTransferIn.newAXFR(new Name(hostname), resolvers.head, null)

      val records: Try[List[Record]] = Try {
        transfer
          .run()
          .asScala
          .toList
          .asInstanceOf[List[org.xbill.DNS.Record]]
          .map(dnsRecord => Record(Type.string(dnsRecord.getType), dnsRecord.rdataToString()))
      }

      records.getOrElse(List.empty)
    }
  }

  def authoritativeNameServers(): Future[List[String]] =
    queryNS().flatMap(nameServersToIPs)

  private def nameServersToIPs(nameServers: List[String]): Future[List[String]] =
    Future.sequence(nameServers.map(_.stripSuffix(".").trim).map(queryA)).map(_.flatten)
}

object DNSLookup {
  case class Record(recordType: String, data: String)

  def forHostname(hostname: String): DNSLookup =
    DNSLookup.forHostnameAndResolvers(hostname, List("8.8.8.8", "8.8.4.4"))

  def forHostnameAndResolver(hostname: String, resolver: String): DNSLookup =
    DNSLookup.forHostnameAndResolvers(hostname, List(resolver))

  def forHostnameAndResolvers(hostname: String, resolvers: List[String]): DNSLookup = {
    new DNSLookup(hostname, resolvers)
  }
}