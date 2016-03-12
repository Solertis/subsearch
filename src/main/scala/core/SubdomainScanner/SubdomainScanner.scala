package core.subdomainscanner

import core.subdomainscanner.DispatcherMessage.NotifyOnCompletion
import output.Logger
import utils.TimeUtils

import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Future}

case class SubdomainScannerArguments(hostname: String,
                                     subdomains: List[String],
                                     resolvers: List[String],
                                     threads: Int,
                                     concurrentResolverRequests: Boolean)

object SubdomainScanner {
  def performScan(arguments: SubdomainScannerArguments, logger: Logger)(implicit ec: ExecutionContext): Future[Unit] = {
    logger.logStartedSubdomainSearch()
    val scanner: SubdomainScanner = new SubdomainScanner(arguments, logger)
    scanner.future.map(_ => None)
  }
}

class SubdomainScanner(arguments: SubdomainScannerArguments, logger: Logger)(implicit ec: ExecutionContext) {
  val system = ActorSystem("SubdomainScanner")
  val listener = system.actorOf(Listener.props(logger), "listener")
  val dispatcher = system.actorOf(Dispatcher.props(arguments, listener), "dispatcher")

  implicit val timeout = TimeUtils.akkaAskTimeout
  val future: Future[Any] = dispatcher ? NotifyOnCompletion

  PauseHandler.create(dispatcher, logger)
}