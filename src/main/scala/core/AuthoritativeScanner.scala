package core

import output.CLIOutput
import connection.DNSLookup
import connection.DNSLookup.Record
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthoritativeScanner(private val hostname: String, private val cli: CLIOutput) {

  private def scan: Future[List[String]] = {
    cli.printWarningWithTime("Attempting zone transfer on authoritative name servers:")

    val dnsLookup = DNSLookup.forHostname(hostname)

    dnsLookup.hostIsValid().flatMap {
      if (_) {
        beginScan(dnsLookup)
      } else {
        cli.printErrorWithTime(s"Error: $hostname has no DNS records.")
        Future(List.empty)
      }
    }
  }

  private def beginScan(dnsLookup: DNSLookup): Future[List[String]] = {
    dnsLookup.authoritativeNameServers()
      .andThen {
        case nameServers =>
          printAuthoritativeNameServers(nameServers.getOrElse(List.empty))
      }
      .flatMap(performZoneTransfers)
      .map(convertRecordDatasToSubdomains)
      .map(filterOutOtherHosts)
      .map(sortRecordsByName)
      .map(printFoundRecords)
      .map(convertRecordsToSubdomains)
  }

  private def printAuthoritativeNameServers(nameServers: List[String]) =
    nameServers.foreach(nameServer => cli.printInfoWithTime(s"Authoritative Name Server - $nameServer"))

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

object AuthoritativeScanner {
  def performScan(hostname: String, cli: CLIOutput): Future[List[String]] = {
    val scanner = new AuthoritativeScanner(hostname, cli)
    scanner.scan
  }
}