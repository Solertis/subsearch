package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.connection.{DNSLookupImpl, DNSLookupTrait}
import com.gilazaria.subsearch.model.RecordType

import scala.concurrent.{ExecutionContext, Future}

class AuthoritativeScanner(private val logger: Logger)(implicit ec: ExecutionContext) {
  private val lookup: DNSLookupTrait = DNSLookupImpl.create()

  def performLookupOnHostname(hostname: String, resolver: String): Future[Set[String]] = {
    logger.logAuthoritativeScanStarted()

    nameserversForHostname(hostname, resolver)
      .flatMap(nameservers => ipsForNameservers(nameservers, resolver))
      .map(printAuthoritativeNameServers)
  }


  private[this] def nameserversForHostname(hostname: String, resolver: String): Future[Set[String]] = {
    Future {
      lookup
        .performQueryOfType(hostname, resolver, RecordType.NS)
        .getOrElse(Set.empty)
        .map(_.data)
    }
  }

  private[this] def ipsForNameservers(nameservers: Set[String], resolver: String): Future[Set[String]] = {
    Future
      .sequence(nameservers.map(ns => ipsForNameserver(ns, resolver)))
      .map(_.flatten)
  }

  private[this] def ipsForNameserver(nameserver: String, resolver: String): Future[Set[String]] = {
    Future {
      lookup
        .performQueryOfType(nameserver, resolver, RecordType.A)
        .getOrElse(Set.empty)
        .map(_.data)
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