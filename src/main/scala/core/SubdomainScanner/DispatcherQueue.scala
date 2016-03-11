package core.subdomainscanner

import scala.collection.mutable
import scala.util.Random

object DispatcherQueue {
  def create(subdomains: List[String], resolvers: List[String], concurrentResolvers: Boolean): DispatcherQueue =
    new DispatcherQueue(subdomains, resolvers, concurrentResolvers)
}

class DispatcherQueue(private val subdomains: List[String],
                      private val resolvers: List[String],
                      private val concurrentResolvers: Boolean) {

  private var allSeenSubdomains: Set[String] = subdomains.toSet

  private val subdomainsQueue: mutable.Queue[String] = mutable.Queue() ++= subdomains
  private val prioritySubdomainsQueue: mutable.Queue[String] = mutable.Queue()
  private val resolversQueue: mutable.Queue[String] = mutable.Queue() ++= Random.shuffle(resolvers).toSet

  def remainingNumberOfSubdomains: Int = subdomainsQueue.size + prioritySubdomainsQueue.size
  def remainingNumberOfResolvers: Int = resolversQueue.size
  def isOutOfSubdomains: Boolean = subdomainsQueue.isEmpty && prioritySubdomainsQueue.isEmpty
  def isOutOfResolvers: Boolean = resolversQueue.isEmpty
  def requeueSubdomain(subdomain: String) = prioritySubdomainsQueue.enqueue(subdomain)

  def recycleResolver(resolver: String) =
    if (!concurrentResolvers) resolversQueue.enqueue(resolver)

  def dequeueResolver(): String =
    if (!concurrentResolvers) resolversQueue.dequeue()
    else resolvers(Random.nextInt(resolvers.size))

  def dequeueSubdomain(): String =
    if (prioritySubdomainsQueue.nonEmpty) prioritySubdomainsQueue.dequeue()
    else subdomainsQueue.dequeue()

  def enqueuePrioritySubdomain(subdomain: String) =
    if (!allSeenSubdomains.contains(subdomain)) {
      prioritySubdomainsQueue.enqueue(subdomain)
      allSeenSubdomains = allSeenSubdomains ++ Set(subdomain)
    }

  def totalNumberOfSubdomains: Int = allSeenSubdomains.size
}
