package com.gilazaria.subsearch.output

import com.gilazaria.subsearch.model.Record

import scala.collection.SortedSet
import scala.concurrent.Future

trait Logger {
  def logHeader(header: String)
  def logConfig(threads: Int, wordlistSize: Int, resolverslistSize: Int)
  def logTarget(hostname: String)
  def logAuthoritativeScanStarted()
  def logAuthoritativeNameServer(nameServer: String)
  def logAuthoritativeScanCompleted()
  def logStartedZoneTransfer()
  def logNameServersNotVulnerableToZoneTransfer()
  def logNameServerVulnerableToZoneTransfer(nameServer: String)
  def logZoneTransferCompleted()
  def logAddingAuthNameServersToResolvers(totalResolversSize: Int)
  def logStartedSubdomainSearch()
  def logTaskCompleted()
  def logTaskFailed()
  def logPausingThreads()
  def logPauseOptions()
  def logInvalidPauseOption()
  def logNotEnoughResolvers()
  def logTimedOutScan(subdomain: String, resolver: String, duration: String)
  def logBlacklistedResolver(resolver: String)
  def logScanCancelled()
  def logLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int)
  def logRecords(records: SortedSet[Record])
  def logRecordsDuringScan(records: SortedSet[Record])

  def completedLoggingFuture: Future[Unit]
}
