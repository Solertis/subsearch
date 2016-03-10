package core

import connection.DNSLookup
import connection.DNSLookup.Record
import output.CLIOutput

import scala.concurrent.{ExecutionContext, Future}

class ZoneTransferScanner(hostname: String, nameServers: List[String], cli: CLIOutput)(implicit ec: ExecutionContext) {

  private def scan(): Future[List[String]] = {
    if (nameServers.size > 1)
      cli.printWarningWithTime("Attempting zone transfers:")
    else
      cli.printWarningWithTime("Attempting zone transfer:")

    performZoneTransfers(nameServers)
      .map(convertRecordDatasToSubdomains)
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
            cli.printSuccessWithTime(s"$nameServer vulnerable to zone transfer!")
      }
  }

  private def convertRecordDatasToSubdomains(records: List[Record]): Set[Record] =
    records.toSet.map(convertRecordDataToSubdomain)

  private def convertRecordDataToSubdomain(record: Record): Record =
    Record(record.recordType, record.name.split(" ").head.stripSuffix(".").trim.toLowerCase)

  private def filterOutOtherHosts(records: Set[Record]): Set[Record] =
    records.filter(_.name.endsWith(hostname))

  private def sortRecordsByName(records: Set[Record]): List[Record] =
    records.toList.sortBy(_.name)

  private def printFoundRecords(records: List[Record]): List[Record] = {
    convertRecordsToSubdomains(records)
      .toSet
      .foreach((subdomain: String) => cli.printFoundSubdomain(subdomain, recordTypesForSubdomainInRecords(subdomain, records)))

    if (records.isEmpty)
      cli.printInfoWithTime("Name servers aren't vulnerable to zone transfer.")

    cli.printLineToCLI()

    records
  }

  private def recordTypesForSubdomainInRecords(subdomain: String, records: List[Record]): List[String] =
    records
      .filter(_.name == subdomain)
      .map(_.recordType)
      .distinct

  private def convertRecordsToSubdomains(records: List[Record]): List[String] =
    records.map(_.name)

}

object ZoneTransferScanner {
  def attemptScan(hostname: String, nameServers: List[String], cli: CLIOutput)(implicit ec: ExecutionContext): Future[List[String]] =
    new ZoneTransferScanner(hostname, nameServers, cli).scan()
}