package output

import pl.project13.scala.rainbow.Rainbow._
import utils.TimeUtils

class CLIOutput {
  def printLineToCLI(string: String): Unit =
    println(string)

  def printLineToCLI(): Unit =
    printLineToCLI("")

  def printHeader(string: String) = {
    printLineToCLI(string.magenta)
    printLineToCLI()
  }

  def printConfig(wordlistSize: Int, resolverslistSize: Int) = {
    val separator = " | ".magenta
    val text = "Wordlist size: ".yellow + wordlistSize.toString.cyan + separator +
               "Number of resolvers: ".yellow + resolverslistSize.toString.cyan
    printLineToCLI(text)
    printLineToCLI()
  }

  def printTarget(hostname: String) = {
    printLineToCLI("Target: ".yellow + hostname.cyan)
    printLineToCLI()
  }

  private def prependTime(string: String): String =
    s"${TimeUtils.currentTimeForCLI} $string"

  def printWarningWithTime(string: String) =
    printWarning(prependTime(string))

  def printWarning(string: String) =
    printLineToCLI(string.yellow)

  def printInfoWithTime(string: String) =
    printInfo(prependTime(string))

  def printInfo(string: String) =
    printLineToCLI(string.blue)

  def printSuccessWithTime(string: String) =
    printSuccess(prependTime(string))

  def printSuccess(string: String) =
    printLineToCLI(string.green)

  def printErrorWithTime(string: String) =
    printError(prependTime(string))

  def printError(string: String) =
    printLineToCLI(string.red)

  def printFoundSubdomain(subdomain: String) =
    printSuccessWithTime(s"Subdomain  -  $subdomain")

}

object CLIOutput {
  def create(): CLIOutput =
    new CLIOutput()
}