package output

import connection.Record
import utils.{HostnameUtils, File}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class StandardOutput(private val file: Option[File], private val verbose: Boolean) extends Output {
  private var saveToFileFuture: Future[Unit] = Future()

  override def print(string: String): Unit =  {
    if (file.isDefined) {
      saveToFileFuture = saveToFileFuture.map {
        _ => file.get.write(string)
      }
    }
  }

  override def writingToFileFuture: Future[Unit] = {
    saveToFileFuture
  }

  override def printRecords(records: List[Record]) = {
    if (verbose) printRecordsVerbose(records)
    else printRecordsNormal(records)
  }

  protected def printRecordsVerbose(records: List[Record]) = {
    val lines: List[String] =
      HostnameUtils
        .distinctAndSortedNames(records)
        .flatMap {
          subdomain =>
            val subdomainRecords: List[Record] = records.filter(_.name == subdomain)
            val recordTypes: List[String] = HostnameUtils.distinctAndSortedTypes(subdomainRecords)

            recordTypes.flatMap {
              recordType =>
                subdomainRecords.filter(_.recordType == recordType).map {
                  case Record(_, _, data) =>
                    val msg = formatRecordTypeAndSubdomainForPrinting(recordType, subdomain)

                    if (List("A", "AAAA", "CNAME", "NS", "SRV").contains(recordType))
                      s"$msg  ->  $data"
                    else if (recordType == "MX")
                      s"$msg  @@  $data"
                    else
                      s"$msg  --  $data"
                }
            }
        }

    if (lines.nonEmpty)
      println(lines.mkString("\n"))
  }

  protected def formatRecordTypeAndSubdomainForPrinting(recordType: String, subdomain: String): String =
    prependTime(f"$recordType%-7s:  $subdomain")

  protected def printRecordsNormal(records: List[Record]) = {
    val lines: List[String] =
      HostnameUtils
        .distinctAndSortedNames(records)
        .map(subdomain => (subdomain, HostnameUtils.recordTypesForSubdomainInRecords(subdomain, records)))
        .map(data => s"${data._2.mkString(", ")}:  ${data._1}")

    if (lines.nonEmpty)
      printSuccess(lines.mkString("\n"))
  }
}

object StandardOutput {
  def create(fileOption: Option[File], verbose: Boolean): Option[StandardOutput] =
    if (fileOption.isDefined) Some(new StandardOutput(fileOption, verbose))
    else None
}
