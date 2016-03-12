package core

import connection.{Record, DNSLookup}
import output.Logger

import scala.concurrent.{ExecutionContext, Future}

class ZoneTransferScanner(hostname: String, nameServers: List[String], logger: Logger)(implicit ec: ExecutionContext) {

  private def scan(): Future[List[String]] = {
    logger.logStartedZoneTransfer()

    performZoneTransfers(nameServers)
      .map(_.distinct)
      .map(filterOutOtherHosts)
      .map(sortRecordsByName)
      .map(printFoundRecords)
      .map(convertRecordsToSubdomains)
  }

  private def performZoneTransfers(nameServers: List[String]): Future[List[Record]] =
    Future
      .sequence(nameServers.map(performZoneTransfer))
      .map(_.flatten)

  private def performZoneTransfer(nameServer: String): Future[List[Record]] = {
    val dnsLookup = DNSLookup.forHostnameAndResolver(hostname, nameServer)
    val zoneTransferRecords: Future[List[Record]] = dnsLookup.zoneTransfer()

    zoneTransferRecords
      .andThen {
        case records =>
          if (records.getOrElse(List.empty).nonEmpty)
            logger.logNameServerVulnerableToZoneTransfer(nameServer)
      }
  }

  private def filterOutOtherHosts(records: List[Record]): List[Record] =
    records.filter(_.name.endsWith(hostname))

  private def sortRecordsByName(records: List[Record]): List[Record] =
    records.sortBy(_.name)

  private def printFoundRecords(records: List[Record]): List[Record] = {
    if (records.isEmpty)
      logger.logNameServersNotVulnerableToZoneTransfer()

    logger.logRecords(records)
    logger.logZoneTransferCompleted()

    records
  }

  private def convertRecordsToSubdomains(records: List[Record]): List[String] =
    records.map(_.name)

}

object ZoneTransferScanner {
  def attemptScan(hostname: String, nameServers: List[String], logger: Logger)(implicit ec: ExecutionContext): Future[List[String]] =
    new ZoneTransferScanner(hostname, nameServers, logger).scan()
}