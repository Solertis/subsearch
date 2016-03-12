import controller.Controller
import core.{ArgumentParser, Arguments}
import output.Logger

class SubSearch(args: Array[String]) {
  val arguments: Arguments = ArgumentParser.parseArguments(args)
  val logger: Logger = Logger.create(arguments.extendedOutput, arguments.csvReportFile)
  val controller: Controller = Controller.create(arguments, logger)
}

object SubSearch {
  def main(args: Array[String]): Unit =
    new SubSearch(args)
}