package core.subdomainscanner

import core.subdomainscanner.ScannerMessage._
import core.subdomainscanner.DispatcherMessage.{AvailableForScan, CompletedScan, PriorityScanSubdomain}
import core.subdomainscanner.ListenerMessage.{FoundSubdomain, LogError}

import akka.actor.{Actor, Props, ActorRef}
import connection.DNSLookup
import scala.concurrent.ExecutionContext.Implicits.global

object Scanner {
  def props(listener: ActorRef, hostname: String): Props =
    Props(new Scanner(listener, hostname))
}

class Scanner(listener: ActorRef, hostname: String) extends Actor {
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
        .forHostnameAndResolver(subdomain, "8.8.4.4") // TODO: Change this to resolvers and iron out bugs
        .queryANY()
        .map(records => self ! ScanComplete(records, subdomain, resolver))

    case ScanComplete(records, subdomain, resolver) =>
      if (records.nonEmpty)
        listener ! FoundSubdomain(subdomain, records)

      records
        .filter(_.recordType == "CNAME")
        .map(_.name)
        .map(_.stripSuffix(".").trim)
        .filter(_.endsWith(hostname))
        .toSet
        .foreach((subdomain: String) => context.parent ! PriorityScanSubdomain(subdomain))

      context.parent ! CompletedScan(subdomain, resolver)

    case e @ _ => listener ! LogError(s"Unknown message $e received by scanner from $sender")
  }
}