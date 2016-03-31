package com.gilazaria.subsearch.core.subdomainscanner

import akka.actor._
import com.gilazaria.subsearch.core.subdomainscanner.DispatcherMessage._
import com.gilazaria.subsearch.core.subdomainscanner.ListenerMessage._
import com.gilazaria.subsearch.core.subdomainscanner.ScannerMessage.{Scan, ScanAvailable}

import scala.concurrent.ExecutionContext

object Dispatcher {
  def props(arguments: SubdomainScannerArguments, listener: ActorRef)(implicit ec: ExecutionContext): Props =
    Props(new Dispatcher(arguments, listener))

  private def createScanners(context: ActorContext, listener: ActorRef, threads: Int, hostname: String)(implicit ec: ExecutionContext): Set[ActorRef] =
    Vector.fill(threads) {
      val scanner = context.actorOf(Scanner.props(listener, hostname))
      context.watch(scanner)
      scanner
    }.toSet
}

class Dispatcher(arguments: SubdomainScannerArguments, listener: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  var master: Option[ActorRef] = None

  var pauseScanning = false
  var numberOfPausedScanners = 0
  var whoToNotifyAboutPaused: Option[ActorRef] = None

  val dispatcherQueue: DispatcherQueue = DispatcherQueue.create(arguments.hostname, arguments.wordlist, arguments.omitSubdomains, arguments.resolvers, arguments.concurrentResolverRequests)

  var scansSoFar: Int = 0

  var currentlyScanning: Set[String] = Set.empty

  var scannerRefs: Set[ActorRef] = Dispatcher.createScanners(context, listener, arguments.threads, arguments.hostname)
  scannerRefs.foreach(_ ! ScanAvailable)

  def receive = {
    case ResumeScanning =>
      scanningHasResumed()
      scannerRefs.foreach(_ ! ScanAvailable)
      listener ! ResumedScanning

    case PauseScanning =>
      scanningHasPaused()
      whoToNotifyAboutPaused = Some(sender)
      listener ! PausingScanning

    case CompletedScan(subdomain, resolver) =>
      subdomainHasBeenScanned(subdomain)
      dispatcherQueue.recycleResolver(resolver)
      scannerIsAvailableToScan(sender)

    case FailedScan(subdomain, resolver) =>
      dispatcherQueue.requeueSubdomain(subdomain)
      dispatcherQueue.blacklistResolver(resolver)
      listener ! BlacklistedResolver(resolver)
      scannerIsAvailableToScan(sender)

    case AvailableForScan =>
      scannerIsAvailableToScan(sender)

    case NotifyOnCompletion =>
      master = Some(sender)

    case PriorityScanSubdomain(subdomain: String) =>
      dispatcherQueue.enqueuePrioritySubdomain(subdomain)

    case Terminated(scanner) =>
      scannerHasTerminated(scanner)

      if (scanningHasNotBeenPaused && allScannersHaveTerminated) {
        if (allSubdomainsHaveBeenScanned) {
          listener ! TaskCompleted(master)
        } else if (dispatcherQueue.isOutOfResolvers) {
          // All resolvers are dead. Scan must terminate
          listener ! TaskFailed(master)
        } else {
          // Add any missed subdomains back to the queue
          currentlyScanning.foreach(_ => dispatcherQueue.requeueSubdomain(_))
          currentlyScanning = Set.empty

          // Start scanning again.
          val numberOfScannersToCreate: Int =
            Array(dispatcherQueue.remainingNumberOfSubdomains,
                  dispatcherQueue.remainingNumberOfResolvers,
                  arguments.threads).min

          scannerRefs = Dispatcher.createScanners(context, listener, numberOfScannersToCreate, arguments.hostname)
          scannerRefs.foreach(_ ! ScanAvailable)
        }
      }
  }

  def scannerIsAvailableToScan(scanner: ActorRef) = {
    if (scanningHasBeenPaused) {
      // Don't send anything to the scanner, consider it paused
      aScannerHasBeenPaused()
    }
    else if (!dispatcherQueue.isOutOfResolvers) {
      val resolver = dispatcherQueue.dequeueResolver()
      val subdomainOpt: Option[String] = dispatcherQueue.dequeueSubdomain()

      if (subdomainOpt.isDefined) {
        val subdomain = subdomainOpt.get

        scanningSubdomain(subdomain)
        scanner ! Scan(subdomain, resolver, 1)
        scansSoFar += 1
        listener ! LastScan(subdomain, scansSoFar, dispatcherQueue.totalNumberOfSubdomains)
      } else {
        // There aren't any subdomains for this scanner. Stop this scanner from working
        terminateScanner(scanner)
      }
    } else {
      // We don't have enough resolvers to go around. Stop this scanner from working
      listener ! NotEnoughResolvers
      terminateScanner(scanner)
    }
  }

  // Keeping track of subdomains currently being scanned
  def subdomainHasBeenScanned(subdomain: String) = currentlyScanning = currentlyScanning.diff(Set(subdomain))
  def scanningSubdomain(subdomain: String) = currentlyScanning = currentlyScanning ++ Set(subdomain)
  def allSubdomainsHaveBeenScanned = dispatcherQueue.isOutOfSubdomains && currentlyScanning.isEmpty

  // Keeping track of scanning and whether it's been paused
  def scanningHasBeenPaused: Boolean = pauseScanning
  def scanningHasNotBeenPaused: Boolean = !pauseScanning
  def aScannerHasBeenPaused() = {
    numberOfPausedScanners += 1

    if (allScannersHaveBeenPaused && whoToNotifyAboutPaused.isDefined)
      whoToNotifyAboutPaused.get ! true
  }
  def allScannersHaveBeenPaused: Boolean = numberOfPausedScanners == scannerRefs.size
  def scanningHasPaused() = pauseScanning = true
  def scanningHasResumed() = {
    pauseScanning = false
    numberOfPausedScanners = 0
  }

  // Keeping track of scanner references
  def scannerHasTerminated(scanner: ActorRef) = scannerRefs = scannerRefs.diff(Set(scanner))
  def terminateScanner(scanner: ActorRef) = context.stop(scanner)
  def allScannersHaveTerminated = scannerRefs.isEmpty
}