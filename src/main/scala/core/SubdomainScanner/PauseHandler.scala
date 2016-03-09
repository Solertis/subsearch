package core.subdomainscanner

import core.subdomainscanner.DispatcherMessage.{ResumeScanning, PauseScanning}
import output.CLIOutput
import utils.TimeUtils

import akka.actor.ActorRef
import akka.pattern.ask
import sun.misc.{Signal, SignalHandler}
import scala.concurrent.Await

// I'll admit, this isn't exactly kosher. Apparently this can't be done in Java 9. That should be fun.
// How to do this in Java was found here: http://twit88.com/blog/2008/02/06/java-signal-handling/

object PauseHandler {
  def create(dispatcher: ActorRef, cli: CLIOutput): PauseHandler =
    new PauseHandler(List("INT"), dispatcher, cli)

  case class InterruptException(msg: String) extends Exception(msg)
  case class ContinueException(msg: String) extends Exception(msg)
}

class PauseHandler(signalNames: List[String], dispatcher: ActorRef, cli: CLIOutput) extends SignalHandler {
  import PauseHandler.{InterruptException, ContinueException}

  private val signalMap = signalNames.map(name => (name, Signal.handle(new Signal(name), this))).toMap

  private var pausingCalled: Boolean = false

  def handle(signal: Signal) = {
    if (pausingCalled)
      exit()
    else
      pausingCalled = true

    implicit val timeout = TimeUtils.akkaAskTimeout
    Await.result(dispatcher ? PauseScanning, TimeUtils.awaitDuration)

    try {
      while (true) {
        cli.printInlineToCLI("[e]xit / [c]ontinue: ")

        val option: String = System.console.readLine().toLowerCase

        if (option == "e")
          throw new InterruptException("Exited the program.")
        else if (option == "c")
          throw new ContinueException("Continuing the scan.")
        else
          cli.printLineToCLI()
      }
    } catch {
      case InterruptException(msg) =>
        exit()
      case ContinueException(msg) =>
        resume()
    }
  }

  private def exit() = {
    cli.printLineToCLI()
    cli.printLineToCLI()
    cli.printError("Cancelled by the user")
    System.exit(0)
  }

  private def resume() = {
    dispatcher ! ResumeScanning
    pausingCalled = false
  }
}