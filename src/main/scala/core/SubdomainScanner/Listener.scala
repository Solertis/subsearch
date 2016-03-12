package core.subdomainscanner

import output.Logger
import core.subdomainscanner.ListenerMessage._

import akka.actor.{ActorRef, Actor, Props}

object Listener {
  def props(logger: Logger) = Props(new Listener(logger))
}

class Listener(logger: Logger) extends Actor {
  def receive = {
    case FoundSubdomain(subdomain, records) =>
      logger.logRecordsDuringScan(records)

    case LastScan(subdomain, requestsSoFar, totalRequests) =>
      logger.logLastRequest(subdomain, requestsSoFar, totalRequests)

    case PausingScanning =>
      logger.logPausingThreads()

    case NotEnoughResolvers =>
      logger.logNotEnoughResolvers()

    case TaskCompleted(master: Option[ActorRef]) =>
      logger.logTaskCompleted()
      if (master.isDefined) master.get ! None
      context.system.terminate()
  }
}