package core.subdomainscanner

import core.Arguments
import core.subdomainscanner.DispatcherMessage.NotifyOnCompletion
import output.CLIOutput
import utils.{SubdomainUtils, TimeUtils}

import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SubdomainScanner {
  def performScan(arguments: Arguments, foundSubdomains: List[String], cli: CLIOutput): Future[Unit] = {
    val threads: Int = arguments.threads
    val resolvers: List[String] = arguments.resolvers.getLines

    val subdomains: List[String] =
      arguments.wordlist.getLines
        .map(SubdomainUtils.normalise)
        .filter(SubdomainUtils.isValid)
        .map(subdomain => SubdomainUtils.ensureSubdomainEndsWithHostname(subdomain, arguments.hostname))
        .diff(foundSubdomains)

    cli.printWarningWithTime("Starting subdomain search:")
    val scanner: SubdomainScanner = new SubdomainScanner(arguments.hostname, threads, subdomains, resolvers, cli)
    scanner
      .future
      .map(_ => cli.printTaskCompleted())
  }
}

class SubdomainScanner(hostname: String, threads: Int, subdomains: List[String], resolvers: List[String], cli: CLIOutput) {
  val system = ActorSystem("SubdomainScanner")
  val listener = system.actorOf(Listener.props(cli), "listener")
  val dispatcher = system.actorOf(Dispatcher.props(listener, hostname, threads, subdomains, resolvers), "dispatcher")

  implicit val timeout = TimeUtils.akkaAskTimeout
  val future: Future[Any] = dispatcher ? NotifyOnCompletion

  PauseHandler.create(dispatcher, cli)
}