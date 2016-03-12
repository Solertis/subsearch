package connection

import java.util

import org.xbill.DNS._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.util.Try

class DNSLookup(private val hostname: String, private val resolvers: List[String]) {
  private val resolver: ExtendedResolver = new ExtendedResolver(resolvers.toArray)

  def hostIsValid(): Future[Boolean] =
    queryANY().map(_.getOrElse(List.empty).nonEmpty)

  def queryANY(): Future[Try[List[Record]]] = queryANY(hostname)
  private def queryANY(hostname: String): Future[Try[List[Record]]] = queryType(Type.ANY, hostname)

  def queryNS(): Future[Try[List[String]]] = queryNS(hostname)
  private def queryNS(hostname: String): Future[Try[List[String]]] = queryType(Type.NS, hostname).map(recordsToData)

  def queryA(): Future[Try[List[String]]] = queryA(hostname)
  private def queryA(hostname: String): Future[Try[List[String]]] = queryType(Type.A, hostname).map(recordsToData)

  private def queryType(recordType: Int, name: String): Future[Try[List[Record]]] = {
    Future {
      val lookup = new Lookup(name, recordType)
      lookup.setResolver(resolver)
      Try {
        Option(lookup.run())
          .getOrElse(Array.empty)
          .toList
          .map(dnsRecord => Record(Type.string(dnsRecord.getType), dnsRecord.getName.toString, dnsRecord.rdataToString))
      }
    }
  }

  private def recordsToData(records: Try[List[Record]]): Try[List[String]] = Try(records.get.map(_.data))

  def zoneTransfer(): Future[List[Record]] = {
    if (resolvers.size != 1)
      throw new IllegalArgumentException("Exactly one resolver must be supplied for a zone transfer.")

    Future {
      val transfer: ZoneTransferIn = ZoneTransferIn.newAXFR(new Name(hostname), resolvers.head, null)

      val records: Try[List[Record]] = Try {
        Option(transfer.run())
          .getOrElse(new util.ArrayList())
          .asScala
          .toList
          .asInstanceOf[List[org.xbill.DNS.Record]]
          .map(dnsRecord => Record(Type.string(dnsRecord.getType), dnsRecord.getName.toString, dnsRecord.rdataToString))
          .filter(dnsRecord => !dnsRecord.name.startsWith(hostname))
      }

      records.getOrElse(List.empty)
    }
  }

  def authoritativeNameServers(): Future[List[String]] =
    queryNS().map(_.getOrElse(List.empty)).flatMap(nameServersToIPs)

  private def nameServersToIPs(nameServers: List[String]): Future[List[String]] =
    Future.sequence(nameServers.map(_.stripSuffix(".").trim).map(queryA).map(_.map(_.getOrElse(List.empty)))).map(_.flatten)
}

object DNSLookup {
  def forHostname(hostname: String): DNSLookup =
    DNSLookup.forHostnameAndResolvers(hostname, List("8.8.8.8", "8.8.4.4"))

  def forHostnameAndResolver(hostname: String, resolver: String): DNSLookup =
    DNSLookup.forHostnameAndResolvers(hostname, List(resolver))

  def forHostnameAndResolvers(hostname: String, resolvers: List[String]): DNSLookup = {
    new DNSLookup(hostname, resolvers)
  }

  def isResolver(resolver: String): Future[Boolean] =
    Future(Try(new SimpleResolver(resolver)).isSuccess)
}

class Record private (val recordType: String, val name: String, val data: String)

object Record {
  def apply(recordType: String, name: String, data: String): Record = {
    val importantData =
      if (recordType == "CNAME")
        data.stripSuffix(".").trim
      else if (recordType == "SRV")
        data.split(" ")(3).stripSuffix(".").trim
      else
        data

    new Record(recordType, name.stripSuffix(".").trim, importantData)
  }

  def unapply(record: Record): Option[(String, String, String)] =
    Some((record.recordType, record.name, record.data))
}