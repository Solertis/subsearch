package core

import controller.Controller
import scopt.OptionParser
import utils.{IPUtils, SubdomainUtils, File}

case class Arguments(hostnames: List[String] = List.empty,
                     wordlist: Option[File] = None,
                     resolvers: List[String] = List.empty,
                     includeAuthoritativeNameServersWithResolvers: Boolean = false,
                     concurrentResolverRequests: Boolean = false,
                     extendedOutput: Boolean = false,
                     threads: Int = 10,
                     skipZoneTransfer: Boolean = false,
                     csvReportFile: Option[File] = None,
                     stdoutReportFile: Option[File] = None)

private class ArgumentParser(private val args: Array[String]) {

  private val ver: String = Controller.version("MAJOR") + "." +
                            Controller.version("MINOR") + "." +
                            Controller.version("REVISION")

  private val parser = new OptionParser[Arguments]("subsearch") {
    head("subsearch", ver)

    note("Options:")

    help("help")
      .text("Prints this usage text.")

    note("")
    note("Mandatory:")

    opt[String]('h', "hostname")
      .valueName("HOSTNAME")
      .text("The hostname(s) to scan. Enter more than one by separating with a comma.")
      .action {
        (argument, config) =>
          val hostnames = argument.split(",").toList.map(SubdomainUtils.normalise)
          hostnames.foreach(verifyHostname)
          config.copy(hostnames = hostnames)
      }
    note("and/or")
    opt[String]('H', "hostlist")
      .valueName("HOSTLIST")
      .text("A file containing a newline delimited list of hostnames to scan.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          verifyFile(file, "hostlist")
          val hostnames = file.getLines.map(SubdomainUtils.normalise)
          hostnames.foreach(verifyHostname)
          config.copy(hostnames = (config.hostnames ++ hostnames).distinct)
      }

    opt[String]('w', "wordlist")
      .required()
      .valueName("WORDLIST")
      .text("A newline delimited list of subdomain names.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          verifyFile(file, "wordlist")
          config.copy(wordlist = Option(file))
      }

    opt[String]('r', "resolvers")
      .valueName("RESOLVERS")
      .text("The name server(s) to scan with. Enter more than one by separating with a comma.")
      .action {
        (argument, config) =>
          val resolvers = argument.split(",").toList.map(IPUtils.normalise)
          resolvers.foreach(verifyResolver)
          config.copy(resolvers = resolvers)
      }
    note("and/or")
    opt[String]('R', "resolverslist")
      .valueName("RESOLVERSLIST")
      .text("A file containing a newline delimited list of name servers to scan with.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          verifyFile(file, "resolvers list")
          val resolvers = file.getLines.map(IPUtils.normalise)
          resolvers.foreach(verifyResolver)
          config.copy(resolvers = (config.resolvers ++ resolvers).distinct)
      }

    note("")
    note("General Settings:")

    opt[Unit]('a', "auth-resolvers")
      .text("Include the hostname's authoritative name servers in the list of resolvers. Defaults to false.")
      .action {
        (argument, config) =>
          config.copy(includeAuthoritativeNameServersWithResolvers = true)
      }

    opt[Unit]('c', "concurrent-resolver-requests")
      .text("Allow for more than one request to each resolver at the same time. If true, it can result in being blacklisted or rate limited by some resolvers. Defaults to false.")
      .action {
        (argument, config) =>
          config.copy(concurrentResolverRequests = true)
      }

    opt[Int]('t', "threads")
      .valueName("THREADCOUNT")
      .text("The number of concurrent threads whilst scanning. Defaults to 10.")
      .action {
        (threads, config) =>
          verifyThreads(threads)
          config.copy(threads = threads)
      }

    opt[Unit]('v', "verbose")
      .text("Show more extended command line output such as the addresses that A, AAAA and CNAME records point to. Defaults to false.")
      .action {
        (argument, config) =>
          config.copy(extendedOutput = true)
      }

    opt[Unit]('z', "no-zone-transfer")
      .text("Avoids attempting a zone transfer against the host's authoritative name servers.")
      .action {
        (argument, config) =>
          config.copy(skipZoneTransfer = true)
      }

    note("")
    note("Reporting:")

    opt[String]("report-csv")
      .valueName("OUTPUTFILE")
      .text("Outputs a CSV report of discovered subdomains including timestamp, subdomain, record type and record data.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          if (!file.isWriteable)
            printErrorThenExit("The output file is not writeble.")
          config.copy(csvReportFile = Some(file))
      }

    opt[String]("report-stdout")
      .valueName("OUTPUTFILE")
      .text("Outputs standard out to a file.")
      .action {
        (argument, config) =>
          val file: File = File.fromFilename(argument)
          if (!file.isWriteable)
            printErrorThenExit("The output file is not writeble.")
          config.copy(stdoutReportFile = Some(file))
      }
  }

  def verifyHostname(hostname: String) =
    if (!SubdomainUtils.isValidDomain(hostname))
      printErrorThenExit("The hostname '$hostname' is invalid.")

  def verifyResolver(resolver: String) =
    if (!IPUtils.isValidIPv4(resolver))
      printErrorThenExit("The resolver '$resolver' is not a valid IPv4 address.")

  def verifyFile(file: File, description : String) = {
    if (!file.exists)
      printErrorThenExit("The " + description + "file does not exist.")
    else if (!file.isFile)
      printErrorThenExit("The " + description + "file is invalid.")
    else if (!file.isReadable)
      printErrorThenExit("The " + description + "file cannot be read.")
  }

  def verifyThreads(threads: Int) = {
    if (threads < 1)
      printErrorThenExit("Threads must be a positive integer.")
  }

  def parse(): Arguments =
    parser.parse(args, Arguments()) match {
      case Some(arguments) =>
        postVerifyArguments(arguments)
        arguments
      case None =>
        System.exit(1)
        Arguments()
    }

  private def printErrorThenExit(message: String) = {
    println("Error: " + message)
    System.exit(1)
  }

  private def postVerifyArguments(arguments: Arguments) = {
    if (arguments.hostnames.isEmpty)
      printErrorThenExit("At least one hostname must be specified.")
    if (arguments.resolvers.isEmpty)
      printErrorThenExit("At least one resolver must be specified.")
  }

}

object ArgumentParser {
  def parseArguments(args: Array[String]): Arguments = {
    val parser = new ArgumentParser(args)
    parser.parse()
  }

}