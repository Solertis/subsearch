package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.connection.DNSLookup
import com.gilazaria.subsearch.model.{Record, RecordType}
import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.utils.TimeUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.collection.SortedSet
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try

class AuthoritativeScannerSpec extends FlatSpec with MockFactory {

  val mockLogger: Logger = mock[Logger]
  val mockLookup: DNSLookup = mock[DNSLookup]
  val mockExecutionContext: ExecutionContext = mock[ExecutionContext]

  behavior of "performLookupOnHostname"

  it should "return the authoritative name servers as ip addresses whilst logging" in {
    val hostname = "domain.com"
    val resolver = "1.1.1.1"

    val nameServerRecords = SortedSet(Record(hostname, RecordType.NS, "ns1.domain.com"), Record(hostname, RecordType.NS, "ns2.domain.com"))
    val nameServerIPs = Map(("ns1.domain.com", "2.2.2.2"), ("ns2.domain.com", "3.3.3.3"))

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    (mockLogger.logAuthoritativeScanStarted _).expects()
    (mockLookup.performQueryOfType _).expects(hostname, resolver, RecordType.NS).returns(Try(nameServerRecords))
    nameServerIPs.keys.foreach {
      ns => (mockLookup.performQueryOfType _).expects(ns, resolver, RecordType.A).returns(Try(SortedSet(Record(ns, RecordType.A, nameServerIPs(ns)))))
    }
    nameServerIPs.values.foreach(ns => (mockLogger.logAuthoritativeNameServer _).expects(ns))
    (mockLogger.logAuthoritativeScanCompleted _).expects()

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expectedIPs = Set("2.2.2.2", "3.3.3.3")
    val actualIPs = Await.result(scanner.performLookupOnHostname(hostname, resolver), TimeUtils.awaitDuration)

    assert(expectedIPs == actualIPs)
  }

  behavior of "ipsForNameServers"

  it should "return an empty set when there are no name servers" in {
    val nameServers = Set.empty[String]
    val resolver = "1.1.1.1"
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected = Set.empty
    val actual = Await.result(scanner.ipsForNameServers(nameServers, resolver), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  it should "return an empty set when all name servers have no A records" in {
    val nameServers = Set("ns1.domain.com", "ns2.domain.com")
    val resolver = "1.1.1.1"
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    nameServers.foreach {
      ns => (mockLookup.performQueryOfType _).expects(ns, resolver, RecordType.A).returns(Try(SortedSet.empty[Record]))
    }

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected = Set.empty
    val actual = Await.result(scanner.ipsForNameServers(nameServers, resolver), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  it should "return a set of IP addresses (one for each A record excluding duplicates) when one or more name servers have A records" in {
    val nameServerTuples =
      Set(
        ("ns1.domain.com", SortedSet(Record("ns1.domain.com.", RecordType.A, "1.1.1.1"), Record("ns1.domain.com.", RecordType.A, "2.2.2.2"))),
        ("ns2.domain.com", SortedSet(Record("ns2.domain.com.", RecordType.A, "2.2.2.2"), Record("ns2.domain.com.", RecordType.A, "3.3.3.3")))
      )
    val nameServers = nameServerTuples.map(tup => tup._1)

    val resolver = "10.10.10.10"
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    nameServerTuples.foreach {
      tup => (mockLookup.performQueryOfType _).expects(tup._1, resolver, RecordType.A).returns(Try(tup._2))
    }

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected: Set[String] = nameServerTuples.map(_._2.map(_.data)).reduce(_ ++ _).toSet
    val actual = Await.result(scanner.ipsForNameServers(nameServers, resolver), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  behavior of "dataFromQuery"

  it should "return an empty set when the hostname has no records" in {
    val nameServer = "sub.domain.com"
    val resolver = "1.1.1.1"
    val recordType = RecordType.A

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    (mockLookup.performQueryOfType _).expects(nameServer, resolver, recordType).returns(Try(SortedSet.empty[Record]))

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected = Set.empty
    val actual = Await.result(scanner.dataFromQuery(nameServer, resolver, recordType), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  it should "return an empty set when the query throws an exception" in {
    val nameServer = "sub.domain.com"
    val resolver = "1.1.1.1"
    val recordType = RecordType.A

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    (mockLookup.performQueryOfType _).expects(nameServer, resolver, recordType).returns(Try(throw new Exception("Test message.")))

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected = Set.empty
    val actual = Await.result(scanner.dataFromQuery(nameServer, resolver, recordType), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  it should "return a set of IP addresses (one for each A record) when the name server has A records" in {
    val nameServer = "ns.domain.com"
    val resolver = "1.1.1.1"
    val recordType = RecordType.A

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val records = SortedSet(Record("ns.domain.com.", RecordType.A, "2.2.2.2"), Record("ns.domain.com.", RecordType.A, "3.3.3.3"))

    (mockLookup.performQueryOfType _).expects(nameServer, resolver, recordType).returns(Try(records))

    val scanner = new AuthoritativeScanner(mockLogger, mockLookup)

    val expected = records.map(_.data).toSet
    val actual = Await.result(scanner.dataFromQuery(nameServer, resolver, recordType), TimeUtils.awaitDuration)

    assert(expected == actual)
  }

  behavior of "printAuthoritativeNameServers"

  it should "log each name server, that the scan has completed, and return the nameservers" in {
    val nameServers = Set("1.1.1.1", "2.2.2.2", "3.3.3.3")

    nameServers.foreach(ns => (mockLogger.logAuthoritativeNameServer _).expects(ns))
    (mockLogger.logAuthoritativeScanCompleted _).expects()

    val scanner = AuthoritativeScanner.create(mockLogger)(mockExecutionContext)

    val actual = scanner.printAuthoritativeNameServers(nameServers)

    assert(nameServers == actual)
  }

  it should "log that the scan has completed, and return an empty set" in {
    (mockLogger.logAuthoritativeScanCompleted _).expects()

    val scanner = AuthoritativeScanner.create(mockLogger)(mockExecutionContext)

    val actual = scanner.printAuthoritativeNameServers(Set.empty)

    assert(Set.empty == actual)
  }

  behavior of "create"

  it should "return an AuthoritativeScanner" in {
    val actualClass = AuthoritativeScanner.create(mockLogger)(mockExecutionContext).getClass
    val expectedClass = classOf[AuthoritativeScanner]

    assert(expectedClass == actualClass)
  }

}
