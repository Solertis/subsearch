package com.gilazaria.subsearch.core.subdomainscanner

import com.gilazaria.subsearch.utils.{HostnameUtils, File}

import scala.collection.mutable
import scala.util.Random

object DispatcherQueue {
  def create(hostname: String,
             wordlist: File,
             omitSubdomains: List[String],
             prioritySubdomains: List[String],
             resolvers: List[String],
             concurrentResolvers: Boolean): DispatcherQueue =
    new DispatcherQueue(hostname, wordlist, omitSubdomains, prioritySubdomains, resolvers, concurrentResolvers)
}

class DispatcherQueue(private val hostname: String,
                      private val wordlist: File,
                      private val omitSubdomains: List[String],
                      private val prioritySubdomains: List[String],
                      private val resolvers: List[String],
                      private val concurrentResolvers: Boolean) {

  private var totalNumberOfScans: Int = wordlist.numberOfLines
  private var scannedSoFar: Int = 0

  private var allSeenSubdomains: Set[String] = omitSubdomains.toSet

  private val subdomainsIterator: Iterator[String] = wordlist.linesIterator
  private val prioritySubdomainsQueue: mutable.Queue[String] = mutable.Queue() ++= prioritySubdomains
  private val resolversQueue: mutable.Queue[String] = mutable.Queue() ++= Random.shuffle(resolvers).toSet

  private var blacklistedResolvers: List[String] = List.empty

  def remainingNumberOfSubdomains: Int = prioritySubdomainsQueue.size
  def remainingNumberOfResolvers: Int = resolversQueue.size
  def isOutOfSubdomains: Boolean = prioritySubdomainsQueue.isEmpty && !subdomainsIterator.hasNext
  def isOutOfResolvers: Boolean = resolversQueue.isEmpty
  def requeueSubdomain(subdomain: String) = prioritySubdomainsQueue.enqueue(subdomain)

  def recycleResolver(resolver: String) =
    if (!concurrentResolvers && !blacklistedResolvers.contains(resolver)) resolversQueue.enqueue(resolver)

  //TODO: This should be a Try because what if all the resolvers are blacklisted and the method is run? Illegal argument exception, that's what.
  def dequeueResolver(): String =
    if (!concurrentResolvers) resolversQueue.dequeue()
    else resolvers.diff(blacklistedResolvers)(Random.nextInt(resolvers.diff(blacklistedResolvers).size))

  /**
    * Getting a new subdomain does NOT add it to the allSeenSubdomains set, as this will quickly
    * take too much memory. We're instead making an assumption that the subdomains word list does not
    * contain repeats.
    */

  def dequeueSubdomain(): Option[String] = {
    scannedSoFar += 1

    if (prioritySubdomainsQueue.nonEmpty) Option(prioritySubdomainsQueue.dequeue())
    else nextSubdomainsIterator()
  }

  private def nextSubdomainsIterator(): Option[String] = {
    if (subdomainsIterator.hasNext) {
      val subdomainPart = HostnameUtils.normalise(subdomainsIterator.next)
      val subdomainPartIsValid = HostnameUtils.isValidSubdomainPart(subdomainPart)
      val fullyQualifiedSubdomain = HostnameUtils.ensureSubdomainEndsWithHostname(subdomainPart, hostname)

      if (subdomainPartIsValid && !allSeenSubdomains.contains(fullyQualifiedSubdomain)) {
        Option(fullyQualifiedSubdomain)
      }
      else {
        scannedSoFar += 1
        nextSubdomainsIterator()
      }
    } else {
      None
    }
  }

  def enqueuePrioritySubdomain(subdomain: String) =
    if (!allSeenSubdomains.contains(subdomain)) {
      prioritySubdomainsQueue.enqueue(subdomain)
      allSeenSubdomains = allSeenSubdomains ++ Set(subdomain)
      totalNumberOfScans += 1
    }

  def totalNumberOfSubdomains: Int =
    totalNumberOfScans

  def blacklistResolver(resolver: String) =
    blacklistedResolvers = (blacklistedResolvers ++ List(resolver)).distinct
}
