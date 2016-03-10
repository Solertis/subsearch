import controller.Controller
import core.{ArgumentParser, Arguments}
import output.CLIOutput

class SubSearch(args: Array[String]) {
  val arguments: Arguments = ArgumentParser.parseArguments(args)
  val cli: CLIOutput = CLIOutput.create()
  val controller: Controller = Controller.create(arguments, cli)
}

object SubSearch {
  def main(args: Array[String]): Unit =
    new SubSearch(args)
}