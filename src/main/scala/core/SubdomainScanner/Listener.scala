package core.subdomainscanner

import output.CLIOutput
import core.subdomainscanner.ListenerMessage._

import akka.actor.{ActorRef, Actor, Props}

object Listener {
  def props(cli: CLIOutput) = Props(new Listener(cli))
}

class Listener(cli: CLIOutput) extends Actor {
  def receive = {
    case FoundSubdomain(subdomain, records) =>
      cli.printFoundSubdomainDuringScan(subdomain, records.map(_.recordType).distinct)

    case PrintWarning(warning: String) =>
      cli.printWarningWithTime(warning)

    case PrintError(error: String) =>
      cli.printErrorWithTime(error)

    case LogError(msg: String) =>
      cli.printInternalErrorWithTime(msg)

    case LastScan(subdomain, requestsSoFar, totalRequests) =>
      cli.printLastRequest(subdomain, requestsSoFar, totalRequests)

    case PausingScanning =>
      cli.printPausingThreads()

    case NotEnoughResolvers =>
      cli.eraseLine()
      cli.printWarningWithTime("There aren't enough resolvers for each thread. Reducing thread count by 1.")

    case TaskCompleted(master: Option[ActorRef]) =>
      cli.printTaskCompleted()
      if (master.isDefined) master.get ! None
      context.system.terminate()
  }
}