package com.gilazaria.subsearch

import com.gilazaria.subsearch.controller.Controller
import com.gilazaria.subsearch.core.{ArgumentParser, Arguments}
import com.gilazaria.subsearch.output.Logger

class SubSearch(args: Array[String]) {
  val arguments: Arguments = ArgumentParser.parseArguments(args)
  val logger: Logger = Logger.create(arguments.extendedOutput, arguments.csvReportFile, arguments.stdoutReportFile)
  val controller: Controller = Controller.create(arguments, logger)
}

object SubSearch {
  def main(args: Array[String]): Unit =
    new SubSearch(args)

  val version = "0.1.x-SNAPSHOT"
}