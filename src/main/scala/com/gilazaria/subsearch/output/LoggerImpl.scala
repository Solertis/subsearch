package com.gilazaria.subsearch.output

import java.util.concurrent.Executors

import com.gilazaria.subsearch.model.Record
import com.gilazaria.subsearch.utils.File
import com.gilazaria.subsearch.utils.MathUtils.percentage

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.SortedSet

class LoggerImpl(private val verbose: Boolean, csvReportFile: Option[File], stdoutReportFile: Option[File])(implicit ec: ExecutionContext) extends Logger {
  private val outputs: Set[Output] =
    Set(
      Some(CLIOutput.create(verbose)),
      StandardOutput.create(stdoutReportFile, verbose),
      CSVOutput.create(csvReportFile)
    ).flatten

  /** Controller **/

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

  /** Authoritative Scanner **/

  def logAuthoritativeScanStarted() =
    outputs.foreach(_.printStatus("Identifying authoritative name servers:"))

  def logAuthoritativeNameServer(nameServer: String) =
    outputs.foreach(_.printSuccess(nameServer))

  def logAuthoritativeScanCompleted() =
    outputs.foreach(_.println())

  def logAddingAuthNameServersToResolvers(totalResolversSize: Int) =
    outputs.foreach {
      output =>
        output.printStatus(s"Adding authoritative name servers to list of resolvers with a total of $totalResolversSize")
        output.println()
    }

  /** Zone Transfer Scanner **/

  def logStartedZoneTransfer() =
    outputs.foreach(_.printStatus("Attempting zone transfer:"))

  def logNameServersNotVulnerableToZoneTransfer() =
    outputs.foreach(_.printInfo("Name servers aren't vulnerable to zone transfer"))

  def logNameServerVulnerableToZoneTransfer(nameServer: String) =
    outputs.foreach(_.printSuccess(s"$nameServer vulnerable to zone transfer!"))

  def logZoneTransferCompleted() =
    outputs.foreach(_.println())

  /** Additional Scanners **/

  override def logAdditionalScansStarted() =
    outputs.foreach(_.printStatus("Launching additional scanners:"))

  override def logAdditionalScannerError(scannerName: String, msg: String) =
    outputs.foreach(_.printInfo(s"$scannerName - Error: $msg"))

  override def logAdditionalScannerFoundSubdomains(scannerName: String, subdomains: Set[String]) =
    outputs.foreach(_.printSuccess(s"$scannerName found ${subdomains.size} possible subdomains."))

  override def logAdditionalScansCompleted() =
    outputs.foreach(_.println())

  /** Subdomain Bruteforce Scanner **/

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
    }

  def logScanForceCancelled() = {
    logScanCancelled()
    outputs.foreach {
      output =>
        if (!completedLoggingFuture.isCompleted && (csvReportFile.isDefined || stdoutReportFile.isDefined))
          output.printErrorWithoutTime("WARNING: Reports may not be complete due to unexpected exit.")
    }
  }

  def logLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int) = {
    val progress: Float = percentage(numberOfRequestsSoFar, totalNumberOfSubdomains)
    outputs.foreach(_.printLastRequest(f"$progress%.2f" + s"% - Last request to: $subdomain"))
  }

  /** Records **/

  def logRecords(records: SortedSet[Record]) = {
    val newRecords = filterOutSeenAndInvalidRecords(records)
    saveNewRecords(newRecords)

    outputs.foreach(_.printRecords(newRecords))
  }

  def logRecordsDuringScan(records: SortedSet[Record]) = {
    val newRecords = filterOutSeenAndInvalidRecords(records)
    saveNewRecords(newRecords)

    outputs.foreach(_.printRecordsDuringScan(newRecords))
  }

  /** Utility **/

  def completedLoggingFuture: Future[Unit] = {
    Future.sequence(outputs.map(_.writingToFileFuture)).map(_ => Unit)
  }

  /** Internal **/

  private var allSeenRecords: SortedSet[Record] = SortedSet.empty
  private def filterOutSeenAndInvalidRecords(records: SortedSet[Record]): SortedSet[Record] =
    records
      .filter(!_.recordType.isOneOf("NSEC", "RRSIG", "SOA"))
      .diff(allSeenRecords)

  private def saveNewRecords(records: SortedSet[Record]) =
    allSeenRecords = allSeenRecords ++ records
}

object LoggerImpl {
  def create(extendedOutput: Boolean, csvReportFile: Option[File], stdoutReportFile: Option[File]): Logger = {
    val executorService = Executors.newFixedThreadPool(1)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    new LoggerImpl(extendedOutput, csvReportFile, stdoutReportFile)(ec)
  }
}