package controller

import java.util.concurrent.Executors

import connection.DNSLookup
import core.{ZoneTransferScanner, AuthoritativeScanner, Arguments}
import core.subdomainscanner.SubdomainScanner

import output.CLIOutput
import utils.FileUtils
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Controller {
  def create(arguments: Arguments, cli: CLIOutput) =
    new Controller(arguments, cli)
}

class Controller(private val arguments: Arguments, private val cli: CLIOutput) {
  val version = Map(("MAJOR",    0),
                    ("MINOR",    1),
                    ("REVISION", 0))

  initialise()

  def initialise() = {
    printBanner()
    printConfig()
    Await.result(runScanForHostname(arguments.hostname), 365.days)
  }

  def printBanner() = {
    val banner: String =
      FileUtils
        .getResourceSource("banner.txt")
        .replaceFirst("MAJOR", version("MAJOR").toString)
        .replaceFirst("MINOR", version("MINOR").toString)
        .replaceFirst("REVISION", version("REVISION").toString)

    cli.printHeader(banner)
  }

  def printConfig() = {
    val wordlistSize = arguments.wordlist.numberOfLines
    val resolversSize = arguments.resolvers.numberOfLines

    cli.printConfig(arguments.threads, wordlistSize, resolversSize)
  }

  def runScanForHostname(hostname: String): Future[Unit] = {
    cli.printTarget(hostname)

    DNSLookup.forHostname(hostname).hostIsValid().flatMap {
      if (_) {
        runScanners(hostname)
      } else {
        cli.printErrorWithTime(s"$hostname has no DNS records.")
        Future()
      }
    }
  }

  private def runScanners(hostname: String): Future[Unit] = {
    val executorService = Executors.newFixedThreadPool(arguments.threads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    val authoritativeNameServers: Future[List[String]] = AuthoritativeScanner.performScan(hostname, cli)

    val zoneTransferResults: Future[(List[String], List[String])] =
      authoritativeNameServers.flatMap {
        nameServers =>
          if (!arguments.skipZoneTransfer)
            ZoneTransferScanner
              .attemptScan(hostname, nameServers, cli)
              .map(results => (nameServers, results))
          else
            Future((nameServers, List.empty))
      }

    zoneTransferResults.flatMap((results: (List[String], List[String])) => SubdomainScanner.performScan(arguments, results._1, results._2, cli))
  }
}
