package core

import output.CLIOutput
import connection.DNSLookup
import scala.concurrent.{ExecutionContext, Future}

class AuthoritativeScanner(private val hostname: String, private val cli: CLIOutput)(implicit ec: ExecutionContext) {

  private def scan(): Future[List[String]] = {
    cli.printWarningWithTime("Identifying authoritative name servers:")

    DNSLookup
      .forHostname(hostname)
      .authoritativeNameServers()
      .map(printAuthoritativeNameServers)
  }

  private def printAuthoritativeNameServers(nameServers: List[String]) = {
    nameServers.foreach(cli.printSuccessWithTime)
    cli.printLineToCLI()
    nameServers
  }
}

object AuthoritativeScanner {
  def performScan(hostname: String, cli: CLIOutput)(implicit ec: ExecutionContext): Future[List[String]] =
    new AuthoritativeScanner(hostname, cli).scan()
}