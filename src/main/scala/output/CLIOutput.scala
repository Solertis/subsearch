package output

import connection.Record
import utils.TimeUtils
import pl.project13.scala.rainbow.Rainbow._

import scala.tools.jline.TerminalFactory

class CLIOutput {

  // CLI
  def printLine(line: String) =
    println(line)

  def printText(text: String) =
    print(text)

  def printLineWithAfterLine(line: String) = {
    printLine(line)
    printNewLine()
  }

  def printLineWithBeforeAndAfterLine(line: String) = {
    printNewLine()
    printLine(line)
    printNewLine()
  }

  def printNewLine() =
    println()

  private def eraseLine() =
    System.out.print("\033[1K\033[0G")

  // Generic
  def printSuccess(string: String) =
    printLine(prependTime(string).green)

  def printStatus(string: String) =
    printStatusWithoutTime(prependTime(string))

  def printStatusWithoutTime(string: String) =
    printLine(string.yellow.bold)

  def printError(string: String) =
    printLine(string.onRed.white.bold)

  def printInfo(string: String) =
    printLine(prependTime(string).blue)

  private def prependTime(string: String): String =
    s"${TimeUtils.currentTimePretty} $string"

  // Application specific
  def printHeader(header: String) = printLineWithAfterLine(header.magenta)

  def printConfig(threads: Int, wordlistSize: Int, resolverslistSize: Int) = {
    val separator = " | ".magenta
    val text = "Threads: ".yellow + threads.toString.green + separator +
      "Wordlist size: ".yellow + wordlistSize.toString.green + separator +
      "Number of resolvers: ".yellow + resolverslistSize.toString.green
    printLineWithAfterLine(text.bold)
  }

  def printTarget(hostname: String) = printLineWithAfterLine(("Target: ".yellow + hostname.green).bold)

  def printTaskCompleted() = printLineWithBeforeAndAfterLine("Task Completed".yellow.bold)

  def printPausingThreads() = {
    eraseLine()
    printStatusWithoutTime("CTRL+C detected: Pausing threads, please wait...")
  }

  lazy val terminal = TerminalFactory.create()
  def printLastRequest(subdomain: String, numberOfRequestsSoFar: Int, totalNumberOfSubdomains: Int) = {
    val percentage: Float = numberOfRequestsSoFar.toFloat / totalNumberOfSubdomains.toFloat * 100
    val message = f"$percentage%.2f" + s"% - Last request to: $subdomain"

    val terminalWidth: Int = terminal.getWidth
    val outputMessage =
      if (message.length > terminalWidth)
        message.substring(0, terminalWidth)
      else
        message

    eraseLine()
    print(outputMessage)
  }

  def printNotEnoughResolvers() = {
    eraseLine()
    printStatus("There aren't enough resolvers for each thread. Reducing thread count by 1.")
  }

  def printRecordsDuringScan(records: List[Record], verbose: Boolean) = {
    eraseLine()
    printRecords(records, verbose)
  }

  def printRecords(records: List[Record], verbose: Boolean) = {
    if (verbose) {
      records
        .map(_.name)
        .distinct
        .sorted
        .foreach {
          subdomain =>
            val subdomainRecords: List[Record] = records.filter(_.name == subdomain)
            val recordTypes: List[String] = subdomainRecords.map(_.recordType).distinct.sorted

            recordTypes.foreach {
              recordType =>
                subdomainRecords.filter(_.recordType == recordType).foreach {
                  case Record(_, _, data) =>
                    val msg = prependTime(f"$recordType%-7s:  $subdomain").green

                    if (List("A", "AAAA", "CNAME", "NS", "SRV").contains(recordType))
                      printLine(s"$msg  ->  $data")
                    else if (recordType == "MX")
                      printLine(s"$msg  @@  $data")
                    else
                      printLine(s"$msg  --  $data")
                }
            }
        }

    } else {
      records
        .map(_.name)
        .distinct
        .sorted
        .map(subdomain => (subdomain, recordTypesForSubdomainInRecords(subdomain, records)))
        .foreach {
          (data: (String, List[String])) =>
            printSuccess(s"${data._2.sorted.mkString(", ")}:  ${data._1}")
        }
    }
  }

  private def recordTypesForSubdomainInRecords(subdomain: String, records: List[Record]): List[String] =
    records
      .filter(_.name == subdomain)
      .map(_.recordType)
      .distinct
}

object CLIOutput {
  def create(): CLIOutput = new CLIOutput()
}