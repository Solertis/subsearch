package core

import scopt.OptionParser
import utils.File

case class Arguments(hostname: String = "",
                     wordlist: File = File.fromFilename("wordlist.txt"),
                     resolvers: File = File.fromFilename("resolvers.txt"))

private class ArgumentParser(private val args: Array[String]) {

  private val parser = new OptionParser[Arguments]("subsearch") {
    head("subsearch", "0.1")

    note("Mandatory:")

    opt[String]('h', "hostname")
      .required()
      .valueName("HOSTNAME")
      .text("The hostname to scan for subdomains.")
      .action { (argument, config) => config.copy(hostname = argument) }

    opt[String]('w', "wordlist")
      .required()
      .valueName("WORDLIST")
      .text("A newline delimited list of subdomain names.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          verifyFile(file, "wordlist")
          config.copy(wordlist = file)
      }

    opt[String]('r', "resolvers")
      .required()
      .valueName("RESOLVERS")
      .text("A newline delimited list of name servers.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          verifyFile(file, "resolvers")
          config.copy(resolvers = File.fromFilename(argument))
      }

    note("Help:")

    help("help")
      .text("Prints this usage text.")
  }

  def verifyFile(file: File, description : String) = {
    if (!file.exists)
      printErrorThenExit("The " + description + "file does not exist.")
    else if (!file.isFile)
      printErrorThenExit("The " + description + "file is invalid.")
    else if (!file.isReadable)
      printErrorThenExit("The " + description + "file cannot be read.")
  }

  def parse(): Arguments =
    parser.parse(args, Arguments()) match {
      case Some(arguments) =>
        arguments
      case None =>
        System.exit(1)
        Arguments()
    }

  private def printErrorThenExit(message: String) = {
    println("Error: " + message)
    System.exit(1)
  }

}

object ArgumentParser {
  def parseArguments(args: Array[String]): Arguments = {
    val parser = new ArgumentParser(args)
    parser.parse()
  }

}