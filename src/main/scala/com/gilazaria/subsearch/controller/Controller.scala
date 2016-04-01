package com.gilazaria.subsearch.controller

import java.util.concurrent.Executors

import com.gilazaria.subsearch.SubSearch
import com.gilazaria.subsearch.connection.DNSLookup
import com.gilazaria.subsearch.core.{Arguments, AuthoritativeScanner, ZoneTransferScanner}
import com.gilazaria.subsearch.core.subdomainscanner.{SubdomainScanner, SubdomainScannerArguments}
import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.utils.{FileUtils, TimeUtils}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

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

    DNSLookup.forHostname(hostname).hostIsValid().flatMap {
      if (_) {
        runScanners(hostname)
      } else {
        logger.logHostnameWithoutDNSRecords(hostname)
        Future(Unit)
      }
    }
  }

  private def runScanners(hostname: String): Future[Unit] = {
    val executorService = Executors.newFixedThreadPool(arguments.threads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

    val authoritativeNameServers: Set[String] = retrieveAuthoritativeNameServers(hostname)

    val zoneTransferSubdomains: Set[String] =
      if (!arguments.zoneTransfer) Set.empty
      else Await.result(ZoneTransferScanner.attemptScan(hostname, authoritativeNameServers.toList, logger), TimeUtils.awaitDuration).toSet

    val resolvers =
      if (arguments.includeAuthoritativeNameServersWithResolvers) (arguments.resolvers ++ authoritativeNameServers).distinct
      else arguments.resolvers

    if (arguments.includeAuthoritativeNameServersWithResolvers)
      logger.logAddingAuthNameServersToResolvers(resolvers.size)

    val subdomainScannerArguments = SubdomainScannerArguments(hostname, arguments.wordlist.get, zoneTransferSubdomains.toList, resolvers, arguments.threads, arguments.concurrentResolverRequests)

    SubdomainScanner.performScan(subdomainScannerArguments, logger)
  }

  private def retrieveAuthoritativeNameServers(hostname: String)(implicit ec: ExecutionContext): Set[String] = {
    val scanner = AuthoritativeScanner.create(logger)
    val lookup = scanner.performLookupOnHostname(hostname, arguments.resolvers.head)
    Await.result(lookup, TimeUtils.awaitDuration)
  }

  def exitGracefully() =
    logger.completedLoggingFuture.andThen { case _ => System.exit(0) }
}
