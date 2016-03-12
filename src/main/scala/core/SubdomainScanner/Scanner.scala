package core.subdomainscanner

import core.subdomainscanner.ScannerMessage._
import core.subdomainscanner.DispatcherMessage.{FailedScan, AvailableForScan, CompletedScan, PriorityScanSubdomain}
import core.subdomainscanner.ListenerMessage.{PrintWarning, FoundSubdomain}

import akka.actor.{Actor, Props, ActorRef}
import connection.DNSLookup
import scala.concurrent.ExecutionContext

object Scanner {
  def props(listener: ActorRef, hostname: String)(implicit ec: ExecutionContext): Props =
    Props(new Scanner(listener, hostname))
}

class Scanner(listener: ActorRef, hostname: String)(implicit ec: ExecutionContext) extends Actor {
  override def postRestart(reason: Throwable) = {
    preStart
    // Reporting for duty after restart
    context.parent ! AvailableForScan
  }

  def receive = {
    case ScanAvailable =>
      // Notified about available work by parent (Subdomain Dispatcher)
      context.parent ! AvailableForScan

    case Scan(subdomain, resolver) =>
      DNSLookup
        .forHostnameAndResolver(subdomain, resolver)
        .queryANY()
        .map(records => self ! ScanComplete(records, subdomain, resolver))

    case ScanComplete(recordsAttempt, subdomain, resolver) =>
      if (recordsAttempt.isSuccess) {
        val records = recordsAttempt.get

        if (records.nonEmpty)
          listener ! FoundSubdomain(subdomain, records)

        records
          .filter(_.recordType == "CNAME")
          .map(_.name)
          .filter(_.endsWith(hostname))
          .distinct
          .foreach((subdomain: String) => context.parent ! PriorityScanSubdomain(subdomain))

        context.parent ! CompletedScan(subdomain, resolver)
      } else {
//        listener ! PrintError(s"Failed to lookup $subdomain with $resolver: ${recordsAttempt.failed.get.getMessage}")
        context.parent ! FailedScan(subdomain, resolver)
      }

    case e @ _ => listener ! PrintWarning(s"Unknown message $e received by scanner from $sender")
  }
}