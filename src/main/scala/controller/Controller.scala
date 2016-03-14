package controller

import java.util.concurrent.Executors

import connection.DNSLookup
import core.{ZoneTransferScanner, AuthoritativeScanner, Arguments}
import core.subdomainscanner.{SubdomainScannerArguments, SubdomainScanner}

import output.Logger
import utils.{SubdomainUtils, TimeUtils, FileUtils}
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Controller {
  def create(arguments: Arguments, logger: Logger) =
    new Controller(arguments, logger)

  val version = Map(("MAJOR",    0),
                    ("MINOR",    1),
                    ("REVISION", 1))
}

class Controller(private val arguments: Arguments, private val logger: Logger) {
  val version = Controller.version

  initialise()

  def initialise() = {
    printHeader()
    printConfig()

    arguments.hostnames.foreach {
      hostname => Await.result(runScanForHostname(hostname), TimeUtils.awaitDuration)
    }

    System.exit(0)
  }

  def printHeader() = {
    val header: String =
      FileUtils
        .getResourceSource("banner.txt")
        .replaceFirst("MAJOR", version("MAJOR").toString)
        .replaceFirst("MINOR", version("MINOR").toString)
        .replaceFirst("REVISION", version("REVISION").toString)

    logger.logHeader(header)
  }

  def printConfig() = {
    val wordlistSize = arguments.wordlist.get.numberOfLines
    val resolversSize = arguments.resolvers.size

    logger.logConfig(arguments.threads, wordlistSize, resolversSize)
  }

  def runScanForHostname(hostname: String): Future[Unit] = {
    logger.logTarget(hostname)

    DNSLookup.forHostname(hostname).hostIsValid().flatMap {
      if (_) {
        runScanners(hostname)
      } else {
        logger.logHostnameWithoutDNSRecords(hostname)
        Future()
      }
    }
  }

  private def runScanners(hostname: String): Future[Unit] = {
    val executorService = Executors.newFixedThreadPool(arguments.threads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    val authoritativeNameServers: List[String] = Await.result(AuthoritativeScanner.performScan(hostname, logger), TimeUtils.awaitDuration)

    val zoneTransferSubdomains: List[String] =
      if (arguments.skipZoneTransfer) List.empty
      else Await.result(ZoneTransferScanner.attemptScan(hostname, authoritativeNameServers, logger), TimeUtils.awaitDuration)

    val resolvers =
      if (arguments.includeAuthoritativeNameServersWithResolvers) (arguments.resolvers ++ authoritativeNameServers).distinct
      else arguments.resolvers

    if (arguments.includeAuthoritativeNameServersWithResolvers)
      logger.logAddingAuthNameServersToResolvers(resolvers.size)

    val subdomainScannerArguments = SubdomainScannerArguments(hostname, arguments.wordlist.get, zoneTransferSubdomains, resolvers, arguments.threads, arguments.concurrentResolverRequests)

    SubdomainScanner.performScan(subdomainScannerArguments, logger)
  }
}
