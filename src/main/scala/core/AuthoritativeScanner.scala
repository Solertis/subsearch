package core

import output.Logger
import connection.DNSLookup
import scala.concurrent.{ExecutionContext, Future}

class AuthoritativeScanner(private val hostname: String, private val logger: Logger)(implicit ec: ExecutionContext) {

  private def scan(): Future[List[String]] = {
    logger.logAuthoritativeScanStarted()

    DNSLookup
      .forHostname(hostname)
      .authoritativeNameServers()
      .map(printAuthoritativeNameServers)
  }

  private def printAuthoritativeNameServers(nameServers: List[String]) = {
    nameServers.foreach(logger.logAuthoritativeNameServer)
    logger.logAuthoritativeScanCompleted()
    nameServers
  }
}

object AuthoritativeScanner {
  def performScan(hostname: String, logger: Logger)(implicit ec: ExecutionContext): Future[List[String]] =
    new AuthoritativeScanner(hostname, logger).scan()
}