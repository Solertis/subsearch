package core.subdomainscanner

import core.subdomainscanner.ScannerMessage._
import core.subdomainscanner.DispatcherMessage.{FailedScan, AvailableForScan, CompletedScan, PriorityScanSubdomain}
import core.subdomainscanner.ListenerMessage.{ScanTimeout, FoundSubdomain}
import utils.TimeoutFuture._

import akka.actor.{Actor, Props, ActorRef}
import connection.DNSLookup
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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

    case Scan(subdomain, resolver, attempt) =>
      val timeout =
        if (attempt == 1) 10.seconds
        else if (attempt == 2) 20.seconds
        else 30.seconds

      DNSLookup
        .forHostnameAndResolver(subdomain, resolver)
        .queryANY()
        .withTimeout(timeout)
        .map(records => self ! ScanComplete(records, subdomain, resolver))
        .recover { case cause => self ! ScanFailed(cause, subdomain, resolver, attempt+1) }

    case ScanComplete(recordsAttempt, subdomain, resolver) =>
      if (recordsAttempt.isSuccess) {
        val records = recordsAttempt.get

        if (records.nonEmpty)
          listener ! FoundSubdomain(subdomain, records)

        records
          .filter(record => List("CNAME", "SRV", "MX").contains(record.recordType))
          .filter(record => record.data.endsWith(hostname))
          .map {
            record =>
              if (record.recordType == "MX") record.data.split(" ").last
              else record.data
          }
          .distinct
          .foreach((subdomain: String) => context.parent ! PriorityScanSubdomain(subdomain))
      } else {
        // Do nothing. This indicates that the DNSLookup class tried three times to lookup the subdomain.
        // For the moment, we aren't going to try again and will mark this subdomain scan as completed.
      }

      context.parent ! CompletedScan(subdomain, resolver)

    case ScanFailed(cause, subdomain, resolver, attempt) =>
      if (attempt < 4) {
        listener ! ScanTimeout(subdomain, resolver, attempt)
        self ! Scan(subdomain, resolver, attempt)
      }
      else {
        context.parent ! FailedScan(subdomain, resolver)
      }
  }
}