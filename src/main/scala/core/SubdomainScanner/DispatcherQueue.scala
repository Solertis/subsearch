package core.subdomainscanner

import scala.collection.mutable

object DispatcherQueue {
  def create(subdomains: List[String], resolvers: List[String]): DispatcherQueue =
    new DispatcherQueue(subdomains, resolvers)
}

class DispatcherQueue(private val subdomains: List[String], private val resolvers: List[String]) {
  private var allSeenSubdomains: Set[String] = subdomains.toSet

  private val subdomainsQueue: mutable.Queue[String] = mutable.Queue() ++= subdomains
  private val prioritySubdomainsQueue: mutable.Queue[String] = mutable.Queue()
  private val resolversQueue: mutable.Queue[String] = mutable.Queue() ++= resolvers.toSet

  def remainingNumberOfSubdomains: Int = subdomainsQueue.size + prioritySubdomainsQueue.size
  def remainingNumberOfResolvers: Int = resolversQueue.size
  def isOutOfSubdomains: Boolean = subdomainsQueue.isEmpty && prioritySubdomainsQueue.isEmpty
  def isOutOfResolvers: Boolean = resolversQueue.isEmpty
  def recycleResolver(resolver: String) = resolversQueue.enqueue(resolver)
  def requeueSubdomain(subdomain: String) = prioritySubdomainsQueue.enqueue(subdomain)
  def dequeueResolver(): String = resolversQueue.dequeue()
  def dequeueSubdomain(): String =
    if (prioritySubdomainsQueue.nonEmpty)
      prioritySubdomainsQueue.dequeue()
    else
      subdomainsQueue.dequeue()
  def enqueuePrioritySubdomain(subdomain: String) =
    if (!allSeenSubdomains.contains(subdomain)) {
      prioritySubdomainsQueue.enqueue(subdomain)
      allSeenSubdomains = allSeenSubdomains ++ Set(subdomain)
    }
}
