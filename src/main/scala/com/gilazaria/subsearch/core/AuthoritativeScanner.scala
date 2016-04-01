package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.connection.{DNSLookupImpl, DNSLookup}
import com.gilazaria.subsearch.model.{Record, RecordType}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.SortedSet

class AuthoritativeScanner(private val logger: Logger)(implicit ec: ExecutionContext) {
  private val lookup: DNSLookup = DNSLookupImpl.create()

  def performLookupOnHostname(hostname: String, resolver: String): Future[Set[String]] = {
    logger.logAuthoritativeScanStarted()

    nameServersForHostname(hostname, resolver)
      .flatMap(nameservers => ipsForNameServers(nameservers, resolver))
      .map(printAuthoritativeNameServers)
  }


  private[this] def nameServersForHostname(hostname: String, resolver: String): Future[Set[String]] = {
    Future {
      lookup
        .performQueryOfType(hostname, resolver, RecordType.NS)
        .getOrElse(SortedSet.empty[Record])
        .map(_.data)
        .toSet
    }
  }

  private[this] def ipsForNameServers(nameservers: Set[String], resolver: String): Future[Set[String]] = {
    Future
      .sequence(nameservers.map(ns => ipsForNameServer(ns, resolver)))
      .map(_.flatten)
  }

  private[this] def ipsForNameServer(nameserver: String, resolver: String): Future[Set[String]] = {
    Future {
      lookup
        .performQueryOfType(nameserver, resolver, RecordType.A)
        .getOrElse(SortedSet.empty[Record])
        .map(_.data)
        .toSet
    }
  }

  private def printAuthoritativeNameServers(nameServers: Set[String]) = {
    nameServers.foreach(logger.logAuthoritativeNameServer)
    logger.logAuthoritativeScanCompleted()
    nameServers
  }
}

object AuthoritativeScanner {
  def create(logger: Logger)(implicit ec: ExecutionContext): AuthoritativeScanner =
    new AuthoritativeScanner(logger)
}