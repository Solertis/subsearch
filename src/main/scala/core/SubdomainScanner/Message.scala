package core.subdomainscanner

import akka.actor.ActorRef
import connection.DNSLookup.Record
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

  case class Scan(subdomain: String, resolver: String)
  case class ScanComplete(records: Try[List[Record]], subdomain: String, resolver: String)
}

object ListenerMessage extends Message {
  case object PausingScanning
  case object ResumedScanning
  case object NotEnoughResolvers

  case class FoundSubdomain(subdomain: String, records: List[Record])
  case class PrintWarning(warning: String)
  case class PrintError(error: String)
  case class LogError(msg: String)
  case class LastScan(subdomain: String, requestsSoFar: Int, totalRequests: Int)
  case class TaskCompleted(master: Option[ActorRef])
}