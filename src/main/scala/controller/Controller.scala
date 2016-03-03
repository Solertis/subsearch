package controller

import core.Arguments
import output.CLIOutput
import utils.{FileUtils, File}

class Controller(private val arguments: Arguments, private val cli: CLIOutput) {
  val version = Map(("MAJOR",    0),
                    ("MINOR",    1),
                    ("REVISION", 0))

  initialise()

  def initialise() = {
    printBanner()
    printConfig()
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

    cli.printConfig(wordlistSize, resolversSize)
  }
}

object Controller {
  def withArgumentsAndCLI(arguments: Arguments, cli: CLIOutput) =
    new Controller(arguments, cli)
}
