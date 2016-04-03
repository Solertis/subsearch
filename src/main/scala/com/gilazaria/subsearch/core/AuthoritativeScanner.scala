package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.connection.{DNSLookupImpl, DNSLookup}
import com.gilazaria.subsearch.model.{Record, RecordType}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.SortedSet

class AuthoritativeScanner private[core] (private val logger: Logger, private val lookup: DNSLookup)(implicit ec: ExecutionContext) {
  def performLookupOnHostname(hostname: String, resolver: String): Future[Set[String]] = {
    logger.logAuthoritativeScanStarted()

    dataFromQuery(hostname, resolver, RecordType.NS)
      .flatMap(nameservers => ipsForNameServers(nameservers, resolver))
      .map(printAuthoritativeNameServers)
  }

  private[core] def ipsForNameServers(nameServers: Set[String], resolver: String): Future[Set[String]] = {
    Future
      .sequence(nameServers.map(ns => dataFromQuery(ns, resolver, RecordType.A)))
      .map(_.flatten)
  }

  private[core] def dataFromQuery(hostname: String, resolver: String, recordType: RecordType): Future[Set[String]] = {
    Future {
      lookup
        .performQueryOfType(hostname, resolver, recordType)
        .getOrElse(SortedSet.empty[Record])
        .map(_.data)
        .toSet
    }
  }

  private[core] def printAuthoritativeNameServers(nameServers: Set[String]) = {
    nameServers.foreach(logger.logAuthoritativeNameServer)
    logger.logAuthoritativeScanCompleted()
    nameServers
  }
}

object AuthoritativeScanner {
  def create(logger: Logger)(implicit ec: ExecutionContext): AuthoritativeScanner =
    new AuthoritativeScanner(logger, DNSLookupImpl.create())
}