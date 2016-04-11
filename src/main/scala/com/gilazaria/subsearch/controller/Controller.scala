package com.gilazaria.subsearch.controller

import java.util.concurrent.Executors

import com.gilazaria.subsearch.SubSearch
import com.gilazaria.subsearch.core.{Arguments, AuthoritativeScanner, ZoneTransferScanner}
import com.gilazaria.subsearch.core.subdomainscanner.{SubdomainScanner, SubdomainScannerArguments}
import com.gilazaria.subsearch.discovery.{DNSDumpsterScanner, Scanner, VirusTotalScanner}
import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.utils.{FileUtils, TimeUtils}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object Controller {
  def create(arguments: Arguments, logger: Logger) =
    new Controller(arguments, logger)
}

class Controller(private val arguments: Arguments, private val logger: Logger) {
  initialise()

  def initialise() = {
    printHeader()
    printConfig()

    arguments.hostnames.foreach {
      hostname => Await.result(runScanForHostname(hostname), TimeUtils.awaitDuration)
    }

    exitGracefully()
  }

  def printHeader() = {
    val header: String =
      FileUtils
        .getResourceSource("banner.txt")
        .replaceFirst("VERSION", SubSearch.version)

    logger.logHeader(header)
  }

  def printConfig() = {
    val wordlistSize = arguments.wordlist.get.numberOfLines
    val resolversSize = arguments.resolvers.size

    logger.logConfig(arguments.threads, wordlistSize, resolversSize)
  }

  def runScanForHostname(hostname: String): Future[Unit] = {
    logger.logTarget(hostname)
    runScanners(hostname)
  }

  private def runScanners(hostname: String): Future[Unit] = {
    val executorService = Executors.newFixedThreadPool(arguments.threads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    val authoritativeNameServers: Set[String] = retrieveAuthoritativeNameServers(hostname)

    val zoneTransferSubdomains: Set[String] = performZoneTransferScan(hostname, authoritativeNameServers)

    val discoveredSubdomains: Set[String] = performAdditionalScans(hostname)

    val resolvers =
      if (arguments.includeAuthoritativeNameServersWithResolvers) (arguments.resolvers ++ authoritativeNameServers).distinct
      else arguments.resolvers

    if (arguments.includeAuthoritativeNameServersWithResolvers)
      logger.logAddingAuthNameServersToResolvers(resolvers.size)

    val subdomainScannerArguments = SubdomainScannerArguments(hostname, arguments.wordlist.get, zoneTransferSubdomains.toList, discoveredSubdomains.toList, resolvers, arguments.threads, arguments.concurrentResolverRequests)

    SubdomainScanner.performScan(subdomainScannerArguments, logger)
  }

  private def retrieveAuthoritativeNameServers(hostname: String)(implicit ec: ExecutionContext): Set[String] = {
    val scanner = AuthoritativeScanner.create(logger)
    val lookup = scanner.performLookupOnHostname(hostname, arguments.resolvers.head)
    Await.result(lookup, TimeUtils.awaitDuration)
  }

  private def performAdditionalScans(hostname: String): Set[String] = {
    val scanners: Set[Scanner] =
      Set(
        DNSDumpsterScanner.conditionallyCreate(arguments.performDNSDumpsterScan),
        VirusTotalScanner.conditionallyCreate(arguments.performVirusTotalScan)
      ).flatten

    if (scanners.isEmpty) return Set.empty

    logger.logAdditionalScansStarted()

    val futures: Set[Future[Set[String]]] =
      scanners.map {
        scanner: Scanner =>
          val subdomains: Future[Set[String]] = scanner
            .scan(hostname)
            .andThen {
              case subdomains: Try[Set[String]] =>
                if (subdomains.isSuccess) logger.logAdditionalScannerFoundSubdomains(scanner.name, subdomains.get)
            }
            .recover {
              case cause: Throwable =>
                logger.logAdditionalScannerError(scanner.name, cause.getMessage)
                Set.empty
            }
          subdomains
      }

    val future: Future[Set[String]] =
      Future
        .sequence(futures)
        .map(_.flatten)
        .andThen { case _ => logger.logAdditionalScansCompleted() }

    Await.result(future, TimeUtils.awaitDuration)
  }

  private def performZoneTransferScan(hostname: String, resolvers: Set[String]): Set[String] =
    if (arguments.performZoneTransfer)
      Await.result(
        ZoneTransferScanner
          .create(logger)
          .performLookup(hostname, resolvers),
        TimeUtils.awaitDuration
      )
    else
      Set.empty

  def exitGracefully() =
    logger.completedLoggingFuture.andThen { case _ => System.exit(0) }
}
