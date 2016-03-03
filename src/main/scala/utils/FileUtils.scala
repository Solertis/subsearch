package utils

import java.nio.file.{Path, Files, Paths}
import scala.collection.JavaConverters._
import scala.io.Source

class File(val path: Path) {
  lazy val numberOfLines: Int = FileUtils.getLines(path).size

  def exists: Boolean =
    FileUtils.exists(path)

  def isFile: Boolean =
    FileUtils.isFile(path)

  def isReadable: Boolean =
    FileUtils.isReadable(path)

  def getLines: List[String] =
    FileUtils.getLines(path)

  def getSource: String =
    getLines.mkString("\n")
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

  def getLines(path: Path): List[String] =
    Files.readAllLines(path).asScala.toList

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
