package utils

import java.nio.charset.StandardCharsets
import java.nio.file._
import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

class File(val path: Path) {
  def exists: Boolean =
    FileUtils.exists(path)

  def isFile: Boolean =
    FileUtils.isFile(path)

  def isReadable: Boolean =
    FileUtils.isReadable(path)

  def linesIterator: Iterator[String] =
    FileUtils.linesIterator(path)

  lazy val getLines: List[String] =
    FileUtils.getLines(path)

  lazy val numberOfLines: Int =
    FileUtils.numberOfLines(path)

  lazy val getSource: String =
    getLines.mkString("\n")

  def isWriteable: Boolean =
    FileUtils.isWriteable(path)

  def write(data: String) =
    FileUtils.writeToPath(data, path)
}

object File {
  def fromFilename(filename: String): File =
    new File(Paths.get(filename))
}

object FileUtils {
  def exists(path: Path): Boolean =
    Files.exists(path)

  def isFile(path: Path): Boolean =
    Files.isRegularFile(path)

  def isReadable(path: Path): Boolean =
    Files.isReadable(path)

  def isWriteable(path: Path): Boolean = {
    if (!FileUtils.exists(path)) {
      val createFileAttempt =
        Try {
          Files.createFile(path)
          Files.delete(path)
        }

      createFileAttempt.isSuccess
    } else {
      Files.isWritable(path)
    }
  }

  def writeToPath(data: String, path: Path) =
    Files.write(path, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND)

  def linesIterator(path: Path): Iterator[String] =
    io.Source.fromFile(path.toUri).getLines

  def getLines(path: Path): List[String] =
    Files.readAllLines(path).asScala.toList

  def numberOfLines(path: Path): Int =
    io.Source.fromFile(path.toUri).getLines.size

  /**
    * This is a hacky way of getting the entire source of a resource
    * into a String object. Thanks to:
    * https://stackoverflow.com/questions/15110315/read-property-file-under-classpath-using-scala
    * and
    * https://stackoverflow.com/questions/18923864/read-all-lines-of-bufferedreader-in-scala-into-a-string
    */
  def getResourceSource(filename: String): String = {
    val reader =
      Source
        .fromInputStream(getClass.getResourceAsStream("/" + filename))
        .bufferedReader()

    Stream
      .continually(reader.readLine())
      .takeWhile(_ != null)
      .mkString("\n")
  }
}
