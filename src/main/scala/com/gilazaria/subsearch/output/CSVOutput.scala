package com.gilazaria.subsearch.output

import com.gilazaria.subsearch.model.Record
import com.gilazaria.subsearch.utils.{File, TimeUtils}

import scala.collection.SortedSet
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CSVOutput(private val file: Option[File]) extends Output {
  override def print(string: String): Unit = {}

  /**
    * Using a future and chaining it means that writing to file will happen on a different thread to printing to CLI
    */

  private var saveToFileFuture: Future[Unit] = Future(Unit)
  override def printRecords(records: SortedSet[Record]) = {
    if (file.isDefined) {
      saveToFileFuture = saveToFileFuture.map {
        _ =>
          val lines = records.map(record => s"${TimeUtils.timestampNow},${record.name},${record.recordType},${record.data}")
          file.get.write(lines.mkString("\n") + "\n")
      }
    }
  }

  override def writingToFileFuture: Future[Unit] = {
    saveToFileFuture
  }
}

object CSVOutput {
  def create(fileOption: Option[File]): Option[CSVOutput] =
    if (fileOption.isDefined) Some(new CSVOutput(fileOption))
    else None
}