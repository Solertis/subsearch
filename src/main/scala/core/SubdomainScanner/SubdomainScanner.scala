package core.subdomainscanner

import connection.DNSLookup
import core.Arguments
import core.subdomainscanner.DispatcherMessage.NotifyOnCompletion
import output.CLIOutput
import utils.{SubdomainUtils, TimeUtils}

import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Await, Future}

object SubdomainScanner {
  def performScan(arguments: Arguments, authoritativeNameServers: List[String], foundSubdomains: List[String], cli: CLIOutput)(implicit ec: ExecutionContext): Future[Unit] = {
    val threads: Int = arguments.threads
    val resolvers: List[String] = computeResolvers(arguments.resolvers.getLines.map(_.trim), authoritativeNameServers, cli)
    val subdomains: List[String] = computeSubdomains(arguments.wordlist.getLines, foundSubdomains, arguments.hostname)

    cli.printWarningWithTime("Starting subdomain search:")
    val scanner: SubdomainScanner = new SubdomainScanner(arguments.hostname, threads, subdomains, resolvers, cli)
    scanner
      .future
      .map(_ => cli.printTaskCompleted())
  }

  private def computeResolvers(suppliedResolvers: List[String], authoritativeNameServers: List[String], cli: CLIOutput)(implicit ec: ExecutionContext): List[String] = {
    cli.printWarningWithTime("Identifying valid resolvers:")

    val resolversFuture: List[Future[(String, Boolean)]] = suppliedResolvers.map {
      resolver =>
        DNSLookup
          .isResolver(resolver)
          .map {
            (isValid: Boolean) =>
              if (!isValid)
                cli.printErrorWithTime(s"$resolver is not a valid resolver")
              (resolver, isValid)
          }
    }

    val resolvers: Future[List[String]] =
      Future
        .sequence(resolversFuture)
        .map(resolvers => resolvers.filter(_._2).map(_._1))
        .map {
          validResolvers =>
            if (validResolvers.size < suppliedResolvers.size)
              cli.printWarningWithTime(s"${validResolvers.size} out of ${suppliedResolvers.size} supplied resolvers are valid.")
            else
              cli.printSuccessWithTime("All resolvers are valid!")

            cli.printLineToCLI()

            (validResolvers ++ authoritativeNameServers).distinct
        }

    Await.result(resolvers, TimeUtils.awaitDuration)
  }

  private def computeSubdomains(suppliedSubdomains: List[String], foundSubdomains: List[String], hostname: String): List[String] =
    suppliedSubdomains
      .map(SubdomainUtils.normalise)
      .filter(SubdomainUtils.isValid)
      .map(subdomain => SubdomainUtils.ensureSubdomainEndsWithHostname(subdomain, hostname))
      .diff(foundSubdomains)
}

class SubdomainScanner(hostname: String, threads: Int, subdomains: List[String], resolvers: List[String], cli: CLIOutput)(implicit ec: ExecutionContext) {
  val system = ActorSystem("SubdomainScanner")
  val listener = system.actorOf(Listener.props(cli), "listener")
  val dispatcher = system.actorOf(Dispatcher.props(listener, hostname, threads, subdomains, resolvers), "dispatcher")

  implicit val timeout = TimeUtils.akkaAskTimeout
  val future: Future[Any] = dispatcher ? NotifyOnCompletion

  PauseHandler.create(dispatcher, cli)
}