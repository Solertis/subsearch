package com.gilazaria.subsearch.connection

import com.gilazaria.subsearch.model
import com.gilazaria.subsearch.model.{Record, RecordType}
import org.xbill.DNS._

import scala.annotation.tailrec
import scala.util.Try
import scala.collection.JavaConverters._

class DNSLookupImpl extends DNSLookupTrait {
  import DNSLookupImpl.{HostNotFoundException, ServerFailureException}

  override def performQueryOfType(hostname: String, resolver: String, recordType: RecordType): Try[Set[model.Record]] =
    if (recordType == RecordType.AXFR)
      Try(performZoneTransfer(hostname, resolver))
    else
      Try(performLookup(hostname, resolver, recordType))

  private[this] def performZoneTransfer(hostname: String, resolver: String): Set[model.Record] = {
    val transfer: ZoneTransferIn = ZoneTransferIn.newAXFR(new Name(hostname), resolver, null)

    recordsFromTransfer(transfer)
      .getOrElse(Set.empty)
      .map(dnsRecord => Record(dnsRecord.getName.toString, Type.string(dnsRecord.getType), dnsRecord.rdataToString))
      .filter(dnsRecord => !dnsRecord.name.startsWith(hostname))
  }

  private[this] def recordsFromTransfer(transfer: ZoneTransferIn): Try[Set[org.xbill.DNS.Record]] = {
    Try {
      Option(transfer.run())
        .map(_.asScala.toSet)
        .getOrElse(Set.empty)
        .asInstanceOf[Set[org.xbill.DNS.Record]]
    }
  }

  private[this] def performLookup(hostname: String, resolver: String, recordType: RecordType): Set[model.Record] = {
    val lookup = new Lookup(hostname, recordType.intValue)
    lookup.setResolver(new SimpleResolver(resolver))
    query(hostname, resolver, lookup)
  }

  @tailrec
  private[this] def query(hostname: String, resolver: String, lookup: Lookup, attempt: Int = 1): Set[model.Record] = {
    lookup.run()

    lookup.getResult match {
      case Lookup.SUCCESSFUL =>
        Option(lookup.getAnswers)
          .map(_.toSet)
          .getOrElse(Set.empty)
          .map(dnsRecord => Record(dnsRecord.getName.toString, Type.string(dnsRecord.getType), dnsRecord.rdataToString))

      case Lookup.HOST_NOT_FOUND =>
        throw new HostNotFoundException(s"The hostname $hostname was not found.")

      case Lookup.UNRECOVERABLE =>
        throw new ServerFailureException(s"There was a data or server error with the resolver $resolver.")

      case Lookup.TYPE_NOT_FOUND =>
        Set.empty

      case Lookup.TRY_AGAIN =>
        if (attempt >= 3) Set.empty
        else query(hostname, resolver, lookup, attempt + 1)
    }
  }
}

object DNSLookupImpl {
  def create(): DNSLookupTrait =
    new DNSLookupImpl()

  case class HostNotFoundException(msg: String) extends Exception(msg)
  case class ServerFailureException(msg: String) extends Exception(msg)
}