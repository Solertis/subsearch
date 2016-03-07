package core

import output.CLIOutput
import connection.DNSLookup
import connection.DNSLookup.Record
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthoritativeScanner(private val hostname: String, private val cli: CLIOutput) {

  private def scan: Future[Unit] = {
    val dnsLookup = DNSLookup.forHostname(hostname)

    dnsLookup.hostIsValid().flatMap {
      if (_) {
        beginScan(dnsLookup)
      } else {
        cli.printErrorWithTime(s"Error: $hostname has no DNS records.")
        Future()
      }
    }
  }

  private def beginScan(dnsLookup: DNSLookup): Future[Unit] = {
    dnsLookup.authoritativeNameServers()
      .andThen {
        case nameServers =>
          printAuthoritativeNameServers(nameServers.getOrElse(List.empty))
      }
      .flatMap(performZoneTransfers)
      .map(convertRecordDatasToSubdomains)
      .map(sortRecordsByName)
      .map(printFoundRecords)
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

  private def sortRecordsByName(records: Set[Record]): List[Record] =
    records.toList.sortBy(_.name)

  private def printFoundRecords(records: List[Record]): Unit =
    records.foreach(cli.printFoundRecord)
}

object AuthoritativeScanner {
  def performScan(hostname: String, cli: CLIOutput): Future[Unit] = {
    val scanner = new AuthoritativeScanner(hostname, cli)
    scanner.scan
  }
}