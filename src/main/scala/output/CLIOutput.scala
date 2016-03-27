package output

import java.io.PrintStream
import connection.Record
import pl.project13.scala.rainbow.Rainbow._
import scala.tools.jline.TerminalFactory

class CLIOutput(private val printStream: PrintStream, private val verbose: Boolean) extends StandardOutput(None, verbose) {
  override def print(string: String) = printStream.print(string)
  private def eraseln() = print("\033[1K\033[0G")

  override def printSuccessWithoutTime(string: String) = println(string.green)
  override def printStatusWithoutTime(string: String) = println(string.yellow.bold)
  override def printInfoWithoutTime(string: String) = println(string.blue)
  override def printErrorWithoutTime(string: String) = println(string.onRed.white.bold)

  override def printStatusDuringScan(string: String) = {
    eraseln()
    super.printStatusDuringScan(string)
    printLastRequest()
  }

  override def printInfoDuringScan(string: String) = {
    eraseln()
    super.printInfoDuringScan(string)
    printLastRequest()
  }

  override def printHeader(header: String) = {
    println(header.magenta)
    println()
  }

  override def printConfig(config: List[(String, String)], separator: String) = {
    val string: String =
      config
        .map((tuple: (String, String)) => tuple._1.yellow + tuple._2.green)
        .mkString(separator.magenta)

    println(string)
    println()
  }

  override def printTarget(text: String, hostname: String) = {
    println(s"${text.yellow}${hostname.green}".bold)
    println()
  }

  override def printTaskCompleted(text: String) = {
    eraseln()
    super.printTaskCompleted(text)
  }

  override def printTaskFailed(text: String) = {
    eraseln()
    super.printTaskFailed(text)
  }

  override def printPausingThreads(text: String) = {
    eraseln()
    printStatusWithoutTime(text)
  }

  override def printPauseOptions(text: String) = {
    print(text)
  }

  override def printInvalidPauseOptions(text: String) = {
    println(text)
  }
  private var lastRequest: String = ""
  override def printLastRequest(text: String) = {
    lastRequest = text
    eraseln()
    printLastRequest()
  }

  lazy val terminal = TerminalFactory.create()
  override def printLastRequest() = {
    val terminalWidth: Int = terminal.getWidth
    val text = if (lastRequest.length <= terminalWidth) lastRequest
               else lastRequest.substring(0, terminalWidth)

    print(lastRequest)
  }

  override def printRecordsDuringScan(records: List[Record]) = {
    eraseln()
    super.printRecordsDuringScan(records)
  }

  override protected def formatRecordTypeAndSubdomainForPrinting(recordType: String, subdomain: String): String =
    super.formatRecordTypeAndSubdomainForPrinting(recordType,subdomain).green
}

object CLIOutput {
  def create(verbose: Boolean): CLIOutput = create(System.out, verbose)
  private[this] def create(printStream: PrintStream, verbose: Boolean): CLIOutput = new CLIOutput(printStream, verbose)
}