package com.gilazaria.subsearch.core.subdomainscanner

import com.gilazaria.subsearch.core.subdomainscanner.DispatcherMessage.NotifyOnCompletion
import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.utils.{File, TimeUtils}

import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Future}

case class SubdomainScannerArguments(hostname: String,
                                     wordlist: File,
                                     omitSubdomains: List[String],
                                     prioritySubdomains: List[String],
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

  private val pauseHandler = PauseHandler.create(dispatcher, logger)
}