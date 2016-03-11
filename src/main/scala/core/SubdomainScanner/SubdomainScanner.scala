package core.subdomainscanner

import core.subdomainscanner.DispatcherMessage.NotifyOnCompletion
import output.CLIOutput
import utils.TimeUtils

import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Await, Future}

case class SubdomainScannerArguments(hostname: String,
                                     subdomains: List[String],
                                     resolvers: List[String],
                                     threads: Int,
                                     concurrentResolverRequests: Boolean)

object SubdomainScanner {
  def performScan(arguments: SubdomainScannerArguments, cli: CLIOutput)(implicit ec: ExecutionContext): Future[Unit] = {
    cli.printWarningWithTime("Starting subdomain search:")
    val scanner: SubdomainScanner = new SubdomainScanner(arguments, cli)
    scanner
      .future
      .map(_ => cli.printTaskCompleted())
  }
}

class SubdomainScanner(arguments: SubdomainScannerArguments, cli: CLIOutput)(implicit ec: ExecutionContext) {
  val system = ActorSystem("SubdomainScanner")
  val listener = system.actorOf(Listener.props(cli), "listener")
  val dispatcher = system.actorOf(Dispatcher.props(arguments, listener), "dispatcher")

  implicit val timeout = TimeUtils.akkaAskTimeout
  val future: Future[Any] = dispatcher ? NotifyOnCompletion

  PauseHandler.create(dispatcher, cli)
}