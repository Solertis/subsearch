package output

import connection.Record
import utils.{TimeUtils, File}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Logger(private val extendedOutput: Boolean, csvReportFile: Option[File]) {
  private val cli: CLIOutput = CLIOutput.create()

  // Printing set formats
  def logHeader(header: String) =
    cli.printHeader(header)

  def logConfig(threads: Int, wordlistSize: Int, resolverslistSize: Int) =
    cli.printConfig(threads, wordlistSize, resolverslistSize)

  def logTarget(hostname: String) =
    cli.printTarget(hostname)

  def logHostnameWithoutDNSRecords(hostname: String) =
    cli.printError(s"$hostname has no DNS records.")

  def logAuthoritativeScanStarted() =
    cli.printSuccess("Identifying authoritative name servers:")

  def logAuthoritativeNameServer(nameServer: String) =
    cli.printSuccess(nameServer)

  def logAuthoritativeScanCompleted() =
    cli.printNewLine()

  def logStartedZoneTransfer() =
    cli.printStatus("Attempting zone transfer:")

  def logNameServersNotVulnerableToZoneTransfer() =
    cli.printInfo("Name servers aren't vulnerable to zone transfer")

  def logNameServerVulnerableToZoneTransfer(nameServer: String) =
    cli.printSuccess(s"$nameServer vulnerable to zone transfer!")

  def logZoneTransferCompleted() =
    cli.printNewLine()

  def logAddingAuthNameServersToResolvers(totalResolversSize: Int) = {
    cli.printStatus(s"Adding authoritative name servers to list of resolvers with a total of $totalResolversSize")
    cli.printNewLine()
  }

  def logStartedSubdomainSearch() =
    cli.printStatus("Starting subdomain search:")

  def logTaskCompleted() =
    cli.printTaskCompleted()

  def logTaskFailed() =
    cli.printTaskFailed()

  def logPausingThreads() =
    cli.printPausingThreads()

  def logPauseOptions() =
    cli.printText("[e]xit / [c]ontinue: ")

  def logInvalidPauseOption() =
    cli.printNewLine()

  def logRecords(records: List[Record]) =
    cli.printRecords(recordNewRecords(records), extendedOutput)

  def logRecordsDuringScan(records: List[Record]) =
    cli.printRecordsDuringScan(recordNewRecords(records), extendedOutput)

  def logLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int) =
    cli.printLastRequest(subdomain, numberOfRequestsSoFar, totalNumberOfSubdomains)

  def logNotEnoughResolvers() =
    cli.printInfoDuringScan("There aren't enough resolvers for each thread. Reducing thread count by 1.")

  def logTimedOutScan(subdomain: String, resolver: String, duration: String) =
    cli.printInfoDuringScan(s"Lookup of $subdomain using $resolver timed out. Increasing timeout to $duration.")

  def logBlacklistedResolver(resolver: String) =
    cli.printInfoDuringScan(s"Lookup using $resolver timed out three times. Blacklisting resolver.")

  def logScanCancelled() = {
    cli.printNewLine()
    cli.printNewLine()
    cli.printError("Cancelled by the user")
  }

  // Keeping track of seen records
  private var allSeenRecords: List[Record] = List.empty
  private def recordNewRecords(records: List[Record]) = {
    val newRecords =
      records
        .filter(dnsRecord => !List("NSEC", "RRSIG", "SOA").contains(dnsRecord.recordType))
        .diff(allSeenRecords)

    allSeenRecords = allSeenRecords ++ records

    if (csvReportFile.isDefined)
      saveRecordsToCSVReportFile(newRecords)

    newRecords
  }

  /**
    * Using a future and chaining it means that writing to file will happen on a different thread to printing to CLI
    */
  private var saveToFileFuture: Future[Unit] = Future()
  private def saveRecordsToCSVReportFile(records: List[Record]) = {
    saveToFileFuture = saveToFileFuture.map {
      _ =>
        val file: File = csvReportFile.get
        val lines = records.map(record => s"${TimeUtils.timestampNow},${record.name},${record.recordType},${record.data}")
        file.writeLines(lines)
    }
  }

}

object Logger {
  def create(extendedOutput: Boolean, csvReportFile: Option[File]): Logger =
    new Logger(extendedOutput: Boolean, csvReportFile: Option[File])
}