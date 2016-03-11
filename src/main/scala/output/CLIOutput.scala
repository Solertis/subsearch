package output

import pl.project13.scala.rainbow.Rainbow._
import utils.TimeUtils
import scala.tools.jline._

class CLIOutput {
  val terminal = TerminalFactory.create()

  def printLineToCLI(string: String): Unit =
    println(string)

  def printInlineToCLI(string: String): Unit =
    print(string)

  def printLineToCLI(): Unit =
    printLineToCLI("")

  def printHeader(string: String) = {
    printLineToCLI(string.magenta)
    printLineToCLI()
  }

  def printConfig(threads: Int, wordlistSize: Int, resolverslistSize: Int) = {
    val separator = " | ".magenta
    val text = "Threads: ".yellow + threads.toString.cyan + separator +
               "Wordlist size: ".yellow + wordlistSize.toString.cyan + separator +
               "Number of resolvers: ".yellow + resolverslistSize.toString.cyan
    printLineToCLI(text.bold)
    printLineToCLI()
  }

  def printTarget(hostname: String) = {
    val text = "Target: ".yellow + hostname.cyan
    printLineToCLI(text.bold)
    printLineToCLI()
  }

  def printTaskCompleted() = {
    printLineToCLI()
    printLineToCLI("Task Completed".yellow.bold)
    printLineToCLI()
  }

  private def prependTime(string: String): String =
    s"${TimeUtils.currentTimeForCLI} $string"

  def printWarningWithTime(string: String) =
    printWarning(prependTime(string))

  def printWarning(string: String) =
    printLineToCLI(string.yellow.bold)

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
    printLineToCLI(string.onRed.white.bold)

  def printInternalErrorWithTime(string: String) =
    printInternalError(prependTime(string))

  def printInternalError(string: String) =
    printLineToCLI(string.red)

  def printFoundSubdomain(subdomain: String, recordTypes: List[String]) =
    printSuccessWithTime(s"${recordTypes.sorted.mkString(", ")}  -  $subdomain")

  def printFoundSubdomainDuringScan(subdomain: String, recordTypes: List[String]) = {
    eraseLine()
    printFoundSubdomain(subdomain, recordTypes)
  }

  def printLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int) = {
    val percentage: Float = numberOfRequestsSoFar.toFloat / totalNumberOfSubdomains.toFloat * 100
    val message = f"$percentage%.2f" + s"% - Last request to: $subdomain"

    val terminalWidth: Int = terminal.getWidth
    val outputMessage =
      if (message.length > terminalWidth)
        message.substring(0, terminalWidth)
      else
        message

    replaceLastLineInCLI(outputMessage)
  }

  def replaceLastLineInCLI(string: String) = {
    eraseLine()
    print(string)
  }

  def eraseLine() = System.out.print("\033[1K\033[0G")

  def printPausingThreads() = {
    eraseLine()
    printWarning("CTRL+C detected: Pausing threads, please wait...")
  }

}

object CLIOutput {
  def create(): CLIOutput =
    new CLIOutput()
}