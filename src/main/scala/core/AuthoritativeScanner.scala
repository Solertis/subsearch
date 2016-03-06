package core

import output.CLIOutput
import connection.DNSLookup
import connection.DNSLookup.Record
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Future}

class AuthoritativeScanner(private val hostname: String, private val cli: CLIOutput) {

  private def scan: Future[Unit] = {
    val dnsLookup = DNSLookup.forHostname(hostname)

    dnsLookup.hostIsValid().flatMap {
      if (_) {
        beginScan(dnsLookup)
      } else {
        cli.printErrorWithTime(s"Error: $hostname has no DNS records.")
        Future {}
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
      .map(_.filter(!_.startsWith(hostname)))
      .map(_.foreach(cli.printFoundSubdomain))
  }

  private def printAuthoritativeNameServers(nameServers: List[String]) =
    nameServers.foreach(nameServer => cli.printInfoWithTime(s"Authoritative Name Server - $nameServer"))

  private def performZoneTransfers(nameServers: List[String]): Future[List[String]] =
    Future
      .sequence(nameServers.map(performZoneTransfer))
      .map(_.flatten)

  private def performZoneTransfer(nameServer: String): Future[List[String]] = {
    val dnsLookup = DNSLookup.forHostnameAndResolver(hostname, nameServer)
    val zoneTransferRecords: Future[List[Record]] = dnsLookup.zoneTransfer()

    zoneTransferRecords
      .andThen {
        case records =>
          if (records.getOrElse(List.empty).nonEmpty)
            cli.printSuccessWithTime(s"$nameServer vulnerable to zone transfer!")
      }
      .map {
        records =>
          records
            .filter(record => record.recordType == "NSEC")
            .map(record => record.data)
      }
  }

  private def convertRecordDatasToSubdomains(recordDatas: List[String]): Set[String] =
    recordDatas.toSet.map((data: String) => data.split(" ").head.stripSuffix(".").trim.toLowerCase)
}

object AuthoritativeScanner {
  def performScan(hostname: String, cli: CLIOutput): Future[Unit] = {
    val scanner = new AuthoritativeScanner(hostname, cli)
    scanner.scan
  }
}