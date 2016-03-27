package output

import utils.TimeUtils
import connection.Record
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Output {
  def print(string: String)
  def println(): Unit = println("")
  def println(string: String): Unit = print(s"$string\n")

  def printSuccess(string: String) = printSuccessWithoutTime(prependTime(string))
  def printStatus(string: String) = printStatusWithoutTime(prependTime(string))
  def printInfo(string: String) = printInfoWithoutTime(prependTime(string))
  def printError(string: String) = printErrorWithoutTime(prependTime(string))

  def printSuccessWithoutTime(string: String) = println(string)
  def printStatusWithoutTime(string: String) = println(string)
  def printInfoWithoutTime(string: String) = println(string)
  def printErrorWithoutTime(string: String) = println(string)

  def printSuccessDuringScan(string: String) = printSuccess(string)
  def printStatusDuringScan(string: String) = printStatus(string)
  def printInfoDuringScan(string: String) = printInfo(string)
  def printErrorDuringScan(string: String) = printError(string)

  // Utility

  final def prependTime(string: String): String =
    s"${TimeUtils.currentTimePretty} $string"

  def writingToFileFuture: Future[Unit] = Future(Unit)

  // Application Specific

  def printHeader(header: String) = {
    println(header)
    println()
  }

  def printConfig(config: List[(String, String)], separator: String) = {
    val string: String =
      config
        .map((tuple: (String, String)) => tuple._1 + tuple._2)
        .mkString(separator)

    println(string)
    println()
  }

  def printTarget(text: String, hostname: String) = {
    println(s"$text$hostname")
    println()
  }

  def printTaskCompleted(text: String) = {
    println()
    printStatusWithoutTime(text)
    println()
  }

  def printTaskFailed(text: String) = {
    println()
    printErrorWithoutTime(text)
    println()
  }

  def printPausingThreads(text: String) = {}

  def printPauseOptions(text: String) = {}

  def printInvalidPauseOptions(text: String) = {}

  def printLastRequest(text: String) = {}

  def printLastRequest() = {}

  def printRecords(records: List[Record])

  def printRecordsDuringScan(records: List[Record]) = printRecords(records)
}
