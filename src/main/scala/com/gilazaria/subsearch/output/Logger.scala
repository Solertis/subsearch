package com.gilazaria.subsearch.output

import com.gilazaria.subsearch.connection.Record
import com.gilazaria.subsearch.utils.File
import com.gilazaria.subsearch.utils.MathUtils.percentage
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Logger(private val verbose: Boolean, csvReportFile: Option[File], stdoutReportFile: Option[File]) {
  private val cli: Option[CLIOutput] = Some(CLIOutput.create(verbose))
  private val stdout: Option[StandardOutput] = StandardOutput.create(stdoutReportFile, verbose)
  private val csv: Option[CSVOutput] = CSVOutput.create(csvReportFile)

  private val outputs: List[Output] = List(cli, csv, stdout).flatten

  def logHeader(header: String) =
    outputs.foreach(_.printHeader(header))

  def logConfig(threads: Int, wordlistSize: Int, resolverslistSize: Int) = {
    val config = List(("Threads: ", threads.toString),
                      ("Wordlist size: ", wordlistSize.toString),
                      ("Number of resolvers: ", resolverslistSize.toString))
    val separator = " | "

    outputs.foreach(_.printConfig(config, separator))
  }

  def logTarget(hostname: String) =
    outputs.foreach(_.printTarget("Target: ", hostname))

  def logHostnameWithoutDNSRecords(hostname: String) =
    outputs.foreach(_.printErrorWithoutTime(s"$hostname has no DNS records."))

  def logAuthoritativeScanStarted() =
    outputs.foreach(_.printStatus("Identifying authoritative name servers:"))

  def logAuthoritativeNameServer(nameServer: String) =
    outputs.foreach(_.printSuccess(nameServer))

  def logAuthoritativeScanCompleted() =
    outputs.foreach(_.println())

  def logStartedZoneTransfer() =
    outputs.foreach(_.printStatus("Attempting zone transfer:"))

  def logNameServersNotVulnerableToZoneTransfer() =
    outputs.foreach(_.printInfo("Name servers aren't vulnerable to zone transfer"))

  def logNameServerVulnerableToZoneTransfer(nameServer: String) =
    outputs.foreach(_.printSuccess(s"$nameServer vulnerable to zone transfer!"))

  def logZoneTransferCompleted() =
    outputs.foreach(_.println())

  def logAddingAuthNameServersToResolvers(totalResolversSize: Int) =
    outputs.foreach {
      output =>
        output.printStatus(s"Adding authoritative name servers to list of resolvers with a total of $totalResolversSize")
        output.println()
    }

  def logStartedSubdomainSearch() =
    outputs.foreach(_.printStatus("Starting subdomain search:"))

  def logTaskCompleted() =
    outputs.foreach(_.printTaskCompleted("Task Completed"))

  def logTaskFailed() =
    outputs.foreach(_.printTaskFailed("Scan aborted as all resolvers are dead."))

  def logPausingThreads() =
    outputs.foreach(_.printPausingThreads("CTRL+C detected: Pausing threads, please wait..."))

  def logPauseOptions() =
    outputs.foreach(_.printPauseOptions("[e]xit / [c]ontinue: "))

  def logInvalidPauseOption() =
    outputs.foreach(_.printInvalidPauseOptions(""))

  def logNotEnoughResolvers() =
    outputs.foreach(_.printInfoDuringScan("There aren't enough resolvers for each thread. Reducing thread count by 1."))

  def logTimedOutScan(subdomain: String, resolver: String, duration: String) =
    outputs.foreach(_.printInfoDuringScan(s"Lookup of $subdomain using $resolver timed out. Increasing timeout to $duration."))

  def logBlacklistedResolver(resolver: String) =
    outputs.foreach(_.printInfoDuringScan(s"Lookup using $resolver timed out three times. Blacklisting resolver."))

  def logScanCancelled() =
    outputs.foreach {
      output =>
        output.println()
        output.println()
        output.printErrorWithoutTime("Cancelled by the user")

        if (!completedLoggingFuture.isCompleted && (csvReportFile.isDefined || stdoutReportFile.isDefined))
          output.printErrorWithoutTime("WARNING: Reports may not be complete due to unexpected exit.")
    }

  def completedLoggingFuture: Future[Unit] = {
    Future.sequence(outputs.map(_.writingToFileFuture)).map(_ => Unit)
  }

  def logLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int) = {
    val progress: Float = percentage(numberOfRequestsSoFar, totalNumberOfSubdomains)
    outputs.foreach(_.printLastRequest(f"$progress%.2f" + s"% - Last request to: $subdomain"))
  }

  def logRecords(records: List[Record]) = {
    val newRecords = filterOutSeenAndInvalidRecords(records)
    saveNewRecords(newRecords)

    outputs.foreach(_.printRecords(newRecords))
  }

  def logRecordsDuringScan(records: List[Record]) = {
    val newRecords = filterOutSeenAndInvalidRecords(records)
    saveNewRecords(newRecords)

    outputs.foreach(_.printRecordsDuringScan(newRecords))
  }

  private var allSeenRecords: List[Record] = List.empty
  private def filterOutSeenAndInvalidRecords(records: List[Record]): List[Record] =
    records
      .filter(dnsRecord => !List("NSEC", "RRSIG", "SOA").contains(dnsRecord.recordType))
      .diff(allSeenRecords)

  private def saveNewRecords(records: List[Record]) =
    allSeenRecords = allSeenRecords ++ records

}

object Logger {
  def create(extendedOutput: Boolean, csvReportFile: Option[File], stdoutReportFile: Option[File]): Logger =
    new Logger(extendedOutput, csvReportFile, stdoutReportFile)
}