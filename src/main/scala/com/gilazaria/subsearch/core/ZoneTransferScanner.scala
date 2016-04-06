package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.connection.{DNSLookupImpl, DNSLookup}
import com.gilazaria.subsearch.model.{Record, RecordType}
import com.gilazaria.subsearch.output.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.collection.SortedSet

class ZoneTransferScanner private[core] (private val logger: Logger, private val lookup: DNSLookup)(implicit ec: ExecutionContext) {
  def performLookup(hostname: String, resolvers: Set[String]): Future[Set[String]] = {
    logger.logStartedZoneTransfer()

    zoneTransfersForHostnameAndResolvers(hostname, resolvers)
      .map(records => recordsEndingWithHostname(hostname, records))
      .map(printFoundRecords)
      .map(namesFromRecords)
  }

  private[core] def zoneTransfersForHostnameAndResolvers(hostname: String, resolvers: Set[String]): Future[SortedSet[Record]] =
    Future
      .sequence(resolvers.map(resolver => zoneTransferForHostnameAndResolver(hostname, resolver)))
      .map(flattenRecords)

  private[core] def zoneTransferForHostnameAndResolver(hostname: String, resolver: String): Future[SortedSet[Record]] = {
    val lookupFut: Future[SortedSet[Record]] =
      Future {
        lookup
          .performQueryOfType(hostname, resolver, RecordType.AXFR)
          .getOrElse(SortedSet.empty[Record])
      }

    lookupFut
      .andThen {
        case records: Try[SortedSet[Record]] =>
          if (records.getOrElse(SortedSet.empty[Record]).nonEmpty)
            logger.logNameServerVulnerableToZoneTransfer(resolver)
      }
  }

  private[core] def flattenRecords(set: Set[SortedSet[Record]]): SortedSet[Record] =
    if (set.isEmpty) SortedSet.empty[Record]
    else set.reduce(_ ++ _)

  private[core] def recordsEndingWithHostname(hostname: String, records: SortedSet[Record]): SortedSet[Record] =
    records.filter(_.name.endsWith(hostname))

  private[core] def printFoundRecords(records: SortedSet[Record]): SortedSet[Record] = {
    if (records.isEmpty)
      logger.logNameServersNotVulnerableToZoneTransfer()
    else
      logger.logRecords(records)

    logger.logZoneTransferCompleted()

    records
  }

  private[core] def namesFromRecords(records: SortedSet[Record]): Set[String] =
    records.map(_.name).toSet

}

object ZoneTransferScanner {
  def create(logger: Logger)(implicit ec: ExecutionContext): ZoneTransferScanner =
    new ZoneTransferScanner(logger, DNSLookupImpl.create())
}