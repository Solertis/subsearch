package controller

import java.util.concurrent.Executors

import connection.DNSLookup
import core.{ZoneTransferScanner, AuthoritativeScanner, Arguments}
import core.subdomainscanner.{SubdomainScannerArguments, SubdomainScanner}

import output.CLIOutput
import utils.{SubdomainUtils, TimeUtils, FileUtils}
import scala.concurrent.{ExecutionContext, Await, Future}
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

    arguments.hostnames.foreach {
      hostname => Await.result(runScanForHostname(hostname), TimeUtils.awaitDuration)
    }
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
    val wordlistSize = arguments.subdomains.size
    val resolversSize = arguments.resolvers.size

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

    val authoritativeNameServers: List[String] = Await.result(AuthoritativeScanner.performScan(hostname, cli), TimeUtils.awaitDuration)

    val zoneTransferSubdomains: List[String] =
      if (arguments.skipZoneTransfer) List.empty
      else Await.result(ZoneTransferScanner.attemptScan(hostname, authoritativeNameServers, cli), TimeUtils.awaitDuration)

    val subdomains =
      arguments.subdomains
        .map(subdomain => SubdomainUtils.ensureSubdomainEndsWithHostname(subdomain, hostname))
        .diff(zoneTransferSubdomains)

    if (arguments.includeAuthoritativeNameServersWithResolvers) {
      cli.printWarningWithTime("Adding authoritative name servers to list of resolvers")
      cli.printLineToCLI()
    }

    val resolvers =
      if (arguments.includeAuthoritativeNameServersWithResolvers) (arguments.resolvers ++ authoritativeNameServers).distinct
      else arguments.resolvers

    val subdomainScannerArguments = SubdomainScannerArguments(hostname, subdomains, resolvers, arguments.threads, arguments.concurrentResolverRequests)

    SubdomainScanner.performScan(subdomainScannerArguments, cli)
  }
}
