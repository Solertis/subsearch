package com.gilazaria.subsearch.connection

import java.util

import com.gilazaria.subsearch.model
import com.gilazaria.subsearch.model.Record
import org.xbill.DNS._

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.util.Try

class DNSLookupOld(private val hostname: String, private val resolvers: List[String]) {
  import DNSLookupOld.HostNotFoundException
  import DNSLookupOld.ServerFailureException

  private val resolver: ExtendedResolver = new ExtendedResolver(resolvers.toArray)

  def hostIsValid(): Future[Boolean] =
    queryType(Type.ANY, hostname).map(_.isSuccess)

  def queryANY(): Future[Try[List[model.Record]]] = queryANY(hostname)
  private def queryANY(hostname: String): Future[Try[List[model.Record]]] = queryType(Type.ANY, hostname)

  def queryNS(): Future[Try[List[String]]] = queryNS(hostname)
  private def queryNS(hostname: String): Future[Try[List[String]]] = queryType(Type.NS, hostname).map(recordsToData)

  private def queryA(hostname: String): Future[Try[List[String]]] = queryType(Type.A, hostname).map(recordsToData)

  private def queryType(recordType: Int, name: String): Future[Try[List[model.Record]]] = {
    @tailrec
    def query(lookup: Lookup, attempt: Int = 1): List[model.Record] = {
      lookup.run()

      lookup.getResult match {
        case Lookup.SUCCESSFUL =>
          Option(lookup.getAnswers)
            .getOrElse(Array.empty)
            .toList
            .map(dnsRecord => Record(dnsRecord.getName.toString, Type.string(dnsRecord.getType), dnsRecord.rdataToString))

        case Lookup.HOST_NOT_FOUND =>
          throw new HostNotFoundException(s"The hostname $name was not found.")

        case Lookup.UNRECOVERABLE =>
          throw new ServerFailureException(s"There was a data or server error with the resolver $resolver.")

        case Lookup.TYPE_NOT_FOUND =>
          List.empty

        case Lookup.TRY_AGAIN =>
          if (attempt == 3) List.empty
          else query(lookup, attempt + 1)
      }
    }

    Future {
      val lookup = new Lookup(name, recordType)
      lookup.setResolver(resolver)
      Try(query(lookup))
    }
  }

  private def recordsToData(records: Try[List[model.Record]]): Try[List[String]] = Try(records.get.map(_.data))

  def zoneTransfer(): Future[List[model.Record]] = {
    if (resolvers.size != 1)
      throw new IllegalArgumentException("Exactly one resolver must be supplied for a zone transfer.")

    Future {
      val transfer: ZoneTransferIn = ZoneTransferIn.newAXFR(new Name(hostname), resolvers.head, null)

      val records: Try[List[model.Record]] = Try {
        Option(transfer.run())
          .getOrElse(new util.ArrayList())
          .asScala
          .toList
          .asInstanceOf[List[org.xbill.DNS.Record]]
          .map(dnsRecord => Record(dnsRecord.getName.toString, Type.string(dnsRecord.getType), dnsRecord.rdataToString))
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

object DNSLookupOld {
  def forHostname(hostname: String): DNSLookupOld =
    DNSLookupOld.forHostnameAndResolvers(hostname, List("8.8.8.8", "8.8.4.4"))

  def forHostnameAndResolver(hostname: String, resolver: String): DNSLookupOld =
    DNSLookupOld.forHostnameAndResolvers(hostname, List(resolver))

  def forHostnameAndResolvers(hostname: String, resolvers: List[String]): DNSLookupOld = {
    new DNSLookupOld(hostname, resolvers)
  }

  def isResolver(resolver: String): Future[Boolean] =
    Future(Try(new SimpleResolver(resolver)).isSuccess)

  case class HostNotFoundException(msg: String) extends Exception(msg)
  case class ServerFailureException(msg: String) extends Exception(msg)
}