package com.gilazaria.subsearch.connection

import com.gilazaria.subsearch.model.{Record, RecordType}
import com.gilazaria.subsearch.connection.LookupTestUtils._
import org.scalatest.FlatSpec
import org.scalamock.scalatest.MockFactory
import org.xbill.DNS.Lookup
import org.xbill.DNS

import scala.collection.SortedSet

class DNSLookupImplSpec extends FlatSpec with MockFactory {
  val lookup: DNSLookupImpl = new DNSLookupImpl()

  behavior of "query"

  val mockLookup: LookupFactory = mock[LookupFactory]

  it should "return the correct records on a successful lookup with records" in {
    val xbillRecords: Array[DNS.Record] =
      Array(
        createDNSARecord("domain.com.", "10.10.10.10"),
        createDNSNSRecord("domain.com.", "ns1.domain.com."),
        createDNSNSRecord("domain.com.", "ns2.domain.com."),
        createDNSSOARecord("domain.com.", "ns1.domain.com. root.domain.com. 118863930 900 900 1800 60")
      )

    val records: Set[Record] =
      Set(
        Record("domain.com.", RecordType.A, "10.10.10.10"),
        Record("domain.com.", RecordType.NS, "ns1.domain.com."),
        Record("domain.com.", RecordType.NS, "ns2.domain.com."),
        Record("domain.com.", RecordType.SOA, "ns1.domain.com. root.domain.com. 118863930 900 900 1800 60")
    )

    (mockLookup.run _).expects
    (mockLookup.getResult _ ).expects().returns(Lookup.SUCCESSFUL)
    (mockLookup.getAnswers _).expects().returns(xbillRecords)

    val expectedRecords = records
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)
  }

  it should "return no records on a successful lookup with no records" in {
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.SUCCESSFUL)
    (mockLookup.getAnswers _).expects().returns(null)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)
  }

  it should "attempt another a query if the first attempt fails" in {
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TRY_AGAIN)
    (mockLookup.run _).expects()
    (mockLookup.getResult _ ).expects().returns(Lookup.SUCCESSFUL)
    (mockLookup.getAnswers _).expects().returns(null)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)
  }

  it should "attempt another a query if the first and second attempt fails" in {
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TRY_AGAIN)
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TRY_AGAIN)
    (mockLookup.run _).expects()
    (mockLookup.getResult _ ).expects().returns(Lookup.SUCCESSFUL)
    (mockLookup.getAnswers _).expects().returns(null)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)

  }

  it should "return an empty set if the third attempt fails" in {
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TRY_AGAIN)
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TRY_AGAIN)
    (mockLookup.run _).expects()
    (mockLookup.getResult _ ).expects().returns(Lookup.TRY_AGAIN)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)

  }
//  it should "throw an exception if the host is not found" in {}
//  it should "throw an exception if the lookup is unrecoverable" in {}
//  it should "return an empty set if the record type is not found" in {}


}
