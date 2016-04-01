package com.gilazaria.subsearch.core.subdomainscanner

import akka.actor.ActorRef
import com.gilazaria.subsearch.model.Record

import scala.collection.SortedSet
import scala.util.Try

trait Message

object DispatcherMessage extends Message {
  case object ResumeScanning
  case object PauseScanning
  case object NotifyOnCompletion
  case object AvailableForScan

  case class PriorityScanSubdomain(subdomain: String)
  case class CompletedScan(subdomain: String, resolver: String)
  case class FailedScan(subdomain: String, resolver: String)
}

object ScannerMessage extends Message {
  case object ScanAvailable

  case class Scan(subdomain: String, resolver: String, attempt: Int)
  case class ScanComplete(records: Try[SortedSet[Record]], subdomain: String, resolver: String)
  case class ScanFailed(cause: Throwable, subdomain: String, resolver: String, attempt: Int)
}

object ListenerMessage extends Message {
  case object PausingScanning
  case object ResumedScanning
  case object NotEnoughResolvers

  case class FoundSubdomain(subdomain: String, records: SortedSet[Record])
  case class LastScan(subdomain: String, requestsSoFar: Int, totalRequests: Int)
  case class TaskCompleted(master: Option[ActorRef])
  case class TaskFailed(master: Option[ActorRef])
  case class ScanTimeout(subdomain: String, resolver: String, attempt: Int)
  case class BlacklistedResolver(resolver: String)
}