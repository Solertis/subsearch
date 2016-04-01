package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.connection.{DNSLookupImpl, DNSLookupTrait}
import com.gilazaria.subsearch.model.{Record, RecordType}
import com.gilazaria.subsearch.output.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.collection.SortedSet

class ZoneTransferScanner(logger: Logger)(implicit ec: ExecutionContext) {
  val lookup: DNSLookupTrait = DNSLookupImpl.create()

  def performLookup(hostname: String, resolvers: Set[String]): Future[Set[String]] = {
    logger.logStartedZoneTransfer()

    zoneTransfersForHostnameAndResolvers(hostname, resolvers)
      .map(records => recordsEndingWithHostname(hostname, records))
      .map(printFoundRecords)
      .map(convertRecordsToSubdomains)
  }

  private def zoneTransfersForHostnameAndResolvers(hostname: String, resolvers: Set[String]): Future[SortedSet[Record]] =
    Future
      .sequence(resolvers.map(resolver => zoneTransferForHostnameAndResolver(hostname, resolver)))
      .map(_.reduce(_ ++ _)) // Flatten for Set[SortedSet[A]]

  private def zoneTransferForHostnameAndResolver(hostname: String, resolver: String): Future[SortedSet[Record]] = {
    val lookupFut: Future[SortedSet[Record]] =
      Future {
        lookup
          .performQueryOfType(hostname, resolver, RecordType.AXFR)
          .getOrElse(SortedSet.empty)
      }

    lookupFut
      .andThen {
        case records: Try[SortedSet[Record]] =>
          if (records.getOrElse(SortedSet.empty).nonEmpty)
            logger.logNameServerVulnerableToZoneTransfer(resolver)
      }
  }

  private def recordsEndingWithHostname(hostname: String, records: SortedSet[Record]): SortedSet[Record] =
    records.filter(_.name.endsWith(hostname))

  private def printFoundRecords(records: SortedSet[Record]): SortedSet[Record] = {
    if (records.isEmpty)
      logger.logNameServersNotVulnerableToZoneTransfer()
    else
      logger.logRecords(records)

    logger.logZoneTransferCompleted()

    records
  }

  private def convertRecordsToSubdomains(records: SortedSet[Record]): Set[String] =
    records.map(_.name).toSet

}

object ZoneTransferScanner {
  def create(logger: Logger)(implicit ec: ExecutionContext): ZoneTransferScanner =
    new ZoneTransferScanner(logger)
}