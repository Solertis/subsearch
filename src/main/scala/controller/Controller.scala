package controller

import core.{AuthoritativeScanner, Arguments}
import core.subdomainscanner.SubdomainScanner

import output.CLIOutput
import utils.FileUtils
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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

    val authoritativeScan: Future[List[String]] =
      if (!arguments.skipZoneTransfer)
        AuthoritativeScanner.performScan(hostname, cli)
      else
        Future(List.empty)

    authoritativeScan.flatMap(foundSubdomains => SubdomainScanner.performScan(arguments, foundSubdomains, cli))
  }
}

object Controller {
  def withArgumentsAndCLI(arguments: Arguments, cli: CLIOutput) =
    new Controller(arguments, cli)
}
