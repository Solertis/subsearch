package com.gilazaria.subsearch.connection

import com.gilazaria.subsearch.connection.DNSLookupImpl.{HostNotFoundException, ServerFailureException}
import com.gilazaria.subsearch.model.{Record, RecordType}
import com.gilazaria.subsearch.connection.LookupTestUtils._
import org.scalatest.FlatSpec
import org.scalamock.scalatest.MockFactory
import org.xbill.DNS.{Lookup, ZoneTransferException}
import org.xbill.DNS
import scala.collection.JavaConverters._

import scala.collection.SortedSet
import scala.util.Try

class DNSLookupImplSpec extends FlatSpec with MockFactory {
  val lookup: DNSLookupImpl = new DNSLookupImpl()

  behavior of "xbillRecordsFromTransfer"

  val mockZoneTransferIn: ZoneTransferInFactory = mock[ZoneTransferInFactory]

  it should "return the correct records on a successful transfer with records" in {
    val hostname = "zonetransfer.me."
    val siphostname = s"_sip._tcp.$hostname"
    val arpahostname = s"157.177.147.217.IN-ADDR.ARPA.$hostname"
    val wwwhostname = s"www.$hostname"

    val expectedRecords: Set[DNS.Record] =
      Set(
        createSOARecord(hostname, "nsztm1.digi.ninja. robin.digi.ninja. 2014101603 172800 900 1209600 3600"),
        createRRSIGRecord(hostname, "SOA 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. GzQojkYAP8zuTOB9UAx66mTDiEGJ26hVIIP2ifk2DpbQLrEAPg4M77i4 M0yFWHpNfMJIuuJ8nMxQgFVCU3yTOeT/EMbN98FYC8lVYwEZeWHtbMmS 88jVlF+cOz2WarjCdyV0+UJCTdGtBJriIczC52EXKkw2RCkv3gtdKKVa fBE="),
        createNSRecord(hostname, "nsztm1.digi.ninja."),
        createNSRecord(hostname, "nsztm2.digi.ninja."),
        createRRSIGRecord(hostname, "NS 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. TyFngBk2PMWxgJc6RtgCE/RhE0kqeWfwhYSBxFxezupFLeiDjHeVXo+S WZxP54Xvwfk7jlFClNZ9lRNkL5qHyxRElhlH1JJI1hjvod0fycqLqCnx XIqkOzUCkm2Mxr8OcGf2jVNDUcLPDO5XjHgOXCK9tRbVVKIpB92f4Qal ulw="),
        createARecord(hostname, "217.147.177.157"),
        createRRSIGRecord(hostname, "A 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. unoMaEPiyoAr0yAWg/coPbAFNznaAlUJW3/QrvJleer50VvGLW/cK+VE DcZLfCu6paQhgJHVddG4p145vVQe3QRvp7EJpUh+SU7dX0I3gngmOa4H k190S4utcXY5FhaN7xBKHVWBlavQaSHTg61g/iuLSB0lS1gp/DAMUpC+ WzE="),
        createHINFORecord(hostname, "\"Casio fx-700G\"", "\"Windows XP\""),
        createRRSIGRecord(hostname, "HINFO 8 2 300 20160330133700 20160229123700 44244 zonetransfer.me. Xebvrpv8nCGn/+iHqok1rcItTPqcskV6jpJ1pCo4WYbnqByLultzygWx JlyVzz+wJHEqRQYDjqGblOdyUgKn2FFnqb1O92kKghcHHvoMEh+Jf5i7 0trtucpRs3AtlneLj2vauOCIEdbjma4IxgdwPahKIhgtgWcUInVFh3Rr SwM="),
        createNSECRecord(hostname, "_sip._tcp.zonetransfer.me. A NS SOA HINFO RRSIG NSEC DNSKEY"),
        createRRSIGRecord(hostname, "NSEC 8 2 3600 20160330133700 20160229123700 44244 zonetransfer.me. MFHBRiIpk14ys3NIb81eCIl8rWULNDAokMeVUlXpwO9OF8jSuFOgPoHy zdAd5NdKe5O/QtuBFizLBTPoR41G5PzQwOZf3cwLEr+35cGsvujwu7Yz H2YU+bkNR/ZvOtYX8hMlM2WxivGyEM+ebZNVn0XcrOI3Kpi31VQ3SGTS Gcs="),
        createDNSKEYRecord(hostname, "256 3 8 AwEAAdXeqhjJnKHczUifC98Nz2xg2xM6DXe0JEE3dNenGNyoByUrxG2m QoZy78l2Pshjc/gKwcTjawHKeI8/aO16xP9nG4pgKyybLPJN9XvJRV5t XFok48g2DPbeu0/TPNoVYALrMeruqxFGv/a/RijzKxrdE3Eqle1906zg nXf43hkL"),
        createDNSKEYRecord(hostname, "256 3 8 AwEAAdd50fHa9wVKIf9/gpOhFx3NBq32WGq/SykRNUwSMnPU3OOkRzUT KEL+yxdoARvL/pbR+5pK3WAOVzsLKdy3+xeLqfILdgY1P0msjVXXNN2o mBV+Hs2Lip7qS4RkYJHg+Oq8RWixpeCSAJ1/DkSLE20HYYiq27a07pNE oc+OfNef"),
        createDNSKEYRecord(hostname, "257 3 8 AwEAAeFLORXsixEH2ftm5oo4VQI66D6zMBreinysTdx5jOVgdAtOa3I6 L3ye7lXZFJbdnPZw0w0df2NvWbIHDzICQxbJjXTUkWuN0wuXm9f6BFP8 VcNa8nPm14F2+6ue+cMxyMOXZW/4vt2DV5hsH+lNWQxGVeTFdCifUlYt sPloxspYETT5vhquKj/VNariRecX0zTzyM67F+grCR+cUo75CnZaRbVZ G7gIBQ783tVO5tFQCUJjqw3zO1f6eQp/xk4rNorCK7BDqitgUbpdPuFh CqmcKVYsFrEViNbaI/Nk9lJHH+nQaYBSZx+cWrTLPZtkNp3ahqY3gLwA 0RUgfYZCWnU="),
        createRRSIGRecord(hostname, "DNSKEY 8 2 300 20160330133700 20160229123700 44244 zonetransfer.me. LWKb719rS66s4b3Th1hCqN1dYsKM2hNTlSoHht7KSngj+zPbXckRQmEQ Dczk0DQO71mnxr8/V3iIHUwlEz54oVJZe/3VvNByL3Hdz88vJfXVjDl2 iPSZV9H+NMvocoWSa+63PinRXdfaSpt5wfd8PtRrO0cy9AZQfZc5Nvzx bvM="),
        createRRSIGRecord(hostname, "DNSKEY 8 2 300 20160330133700 20160229123700 47530 zonetransfer.me. zrmeIf1WdtuwrZgeJRgnP6SOLC+MXiw4UKUEvdA8bhdw1uHEAV8onMWO xQgHWOXV3/npzGhwTcLMoO6NqrV8dZyjL7hgo9PafECNWRIJmtEVOLhd uCiqd5fWFWabiqMr5fHkYIDocnjoz5iyau2ARhIun4U/sygKOPkVTAzP UhvywrNAp3vS+pSCTpPhybXuyU46dIKkZ0XAAwprfR4zT1utsSeafsK8 LGmf4RkmKZXO8nXiz2LKcBbrHv1JM9OvuNTYukI15RSyMDFRhfRCn+ZC UG12rJvWOg5zViaIK/phI5nNd281ghWvFs6Xb9/MJ3f3hWopDOk/ALel brPQ7g=="),
        createSRVRecord(siphostname, "0 0 5060 www.zonetransfer.me."),
        createRRSIGRecord(siphostname, "SRV 8 4 14000 20160330133700 20160229123700 44244 zonetransfer.me. trlrzT1EGDJ+I0GwCcCS1FU9HDiR+y9NOV3g+tyygeOUQDIE4Yr/UTvT e/UEjSEbluMOwAAPuMIFV9amchsbTBLIzxtVsZgPscJMB8vIA4UVSFOq NsuPbFd1uBsPJ9gJYjf7/L8hnZ3r6KDXxdOzHWhs/RhMKV9/pV+UMBzT fm0="),
        createNSECRecord(siphostname, "157.177.147.217.IN-ADDR.ARPA.zonetransfer.me. SRV RRSIG NSEC"),
        createRRSIGRecord(siphostname, "NSEC 8 4 3600 20160330133700 20160229123700 44244 zonetransfer.me. zxoMlL21uMJDNv54lggFbbp13q001YAqXobmIGBKyJR7fjD5vLNs54s7 nOLbTMee9rjzz6fMLUrNscPKh+Io9K8vSLhGK/SQbip5d/uHkITj0OW1 10QZ800ZbM5apzxVEg60expe7PmcbxlunEqGLr9+aO377DSADkDagXhB oQA="),
        createPTRRecord(arpahostname, "www.zonetransfer.me."),
        createRRSIGRecord(arpahostname, "PTR 8 8 7200 20160330133700 20160229123700 44244 zonetransfer.me. NLvoxfZKnVcap87fQs52MaUVX3Yi0pyQCrPJDkNNDIWvB78NHy6eNSZt 4cIb/oUgQW8Gw6/soXrMXBwJcG6gaVNXPEph1v8Yiobm3fyetGgeCSB8 rnhQwfCltiW1zCpIvkDZKIy7FgTLOSr+RjnPZA/cMu4c9QsJLgYXyytL hWs="),
        createNSECRecord(arpahostname, "asfdbauthdns.zonetransfer.me. PTR RRSIG NSEC"),
        createRRSIGRecord(arpahostname, "NSEC 8 8 3600 20160330133700 20160229123700 44244 zonetransfer.me. jU2Gm0tnY2m+JF8q2ca7U03I6HWSsCzGGQoYEzla+7QebAs9ho66PpAQ RdmPKX5KNIYZJSop+492a/1CBT/WR0hqUAgVS/UOuutCyrHqpeVWnl7x tx5CujVs1+Mn48RlGBzanTDzhRr44h/4nNJtZM3/OTWBkpnpakBWn/W6 EdI="),
        createARecord(wwwhostname, "217.147.177.157"),
        createRRSIGRecord(wwwhostname, "A 8 3 7200 20160330133700 20160229123700 44244 zonetransfer.me. tVFIQ2rklljhe64SM8JAn740XSk+MIEXuJJQ9u6Qzmti058hA0hEzdrz 7nAt/LmMcC9RCFTAOl8CSewVExhHgiwu1sO5i69jpI0G87eecAYnGnJe Cd9pf0Wfvm+3ucIxOKm59YmHXOF/48dJlyoKnXNEg6nCBzoW9aq3OreO 16s="),
        createNSECRecord(wwwhostname, "xss.zonetransfer.me. A RRSIG NSEC"),
        createRRSIGRecord(wwwhostname, "NSEC 8 3 3600 20160330133700 20160229123700 44244 zonetransfer.me. 0xCqc6tWcT11ACD24Ap68hc7HRyAcCf7MrkDqe2HyYMGuGS9YSwosiF3 QzffhuY5qagIFbpI3f7xVGxykngThTk37/JO2SrfI7Z5kvqLHdEd6GD9 sogsLqTfHE9UToOYYfuasO+IsJLyPALh89yk3bY+NipvpEPngSnxN6eh Ikc="),
        createSOARecord(hostname, "nsztm1.digi.ninja. robin.digi.ninja. 2014101603 172800 900 1209600 3600")
      )

    (mockZoneTransferIn.run _).expects().returns(expectedRecords.toList.asJava)

    val actual: Try[Set[DNS.Record]] = lookup.xbillRecordsFromTransfer(mockZoneTransferIn)

    assert(actual.isSuccess)
    assert(expectedRecords == actual.get)
  }

  it should "return no records on a successful transfer with no records" in {
    (mockZoneTransferIn.run _).expects().returns(null)

    val expectedRecords: Set[DNS.Record] = Set.empty
    val actual: Try[Set[DNS.Record]] = lookup.xbillRecordsFromTransfer(mockZoneTransferIn)

    assert(actual.isSuccess)
    assert(expectedRecords == actual.get)
  }

  it should "return a failed try on a failed transfer" in {
    (mockZoneTransferIn.run _).expects().throws(new ZoneTransferException("Test message."))

    val expectedException = intercept[ZoneTransferException] {
      lookup.xbillRecordsFromTransfer(mockZoneTransferIn).get
    }

    assert(expectedException.getMessage == "Test message.")
  }

  behavior of "query"

  val mockLookup: LookupFactory = mock[LookupFactory]

  it should "return the correct records on a successful lookup with records" in {
    val xbillRecords: Array[DNS.Record] =
      Array(
        createARecord("domain.com.", "10.10.10.10"),
        createNSRecord("domain.com.", "ns1.domain.com."),
        createNSRecord("domain.com.", "ns2.domain.com."),
        createSOARecord("domain.com.", "ns1.domain.com. root.domain.com. 118863930 900 900 1800 60")
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

  it should "throw an exception if the host is not found" in {
    val hostname = "domain.com"

    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.HOST_NOT_FOUND)

    val expectedException = intercept[HostNotFoundException] {
      lookup.query(hostname, "", mockLookup)
    }

    assert(expectedException.getMessage == s"The hostname $hostname was not found.")
  }

  it should "throw an exception if the lookup is unrecoverable" in {
    val resolver = "10.10.10.10"

    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.UNRECOVERABLE)

    val expectedException = intercept[ServerFailureException] {
      lookup.query("", resolver, mockLookup)
    }

    assert(expectedException.getMessage == s"There was a data or server error with the resolver $resolver.")
  }

  it should "return an empty set if the record type is not found" in {
    (mockLookup.run _).expects()
    (mockLookup.getResult _).expects().returns(Lookup.TYPE_NOT_FOUND)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = lookup.query("", "", mockLookup)

    assert(expectedRecords == actualRecords)
  }

  behavior of "recordsFromXbillRecords"

  it should "convert DNS Java records to subsearch records" in {
    val xbillRecords: Set[DNS.Record] =
      Set(
        createARecord("domain.com.", "10.10.10.10"),
        createNSRecord("domain.com.", "ns1.domain.com."),
        createNSRecord("domain.com.", "ns2.domain.com."),
        createSOARecord("domain.com.", "ns1.domain.com. root.domain.com. 118863930 900 900 1800 60")
      )

    val expectedRecords: SortedSet[Record] =
      SortedSet(
        Record("domain.com.", RecordType.A, "10.10.10.10"),
        Record("domain.com.", RecordType.NS, "ns1.domain.com."),
        Record("domain.com.", RecordType.NS, "ns2.domain.com."),
        Record("domain.com.", RecordType.SOA, "ns1.domain.com. root.domain.com. 118863930 900 900 1800 60")
      )

    val actualRecords = lookup.recordsFromXbillRecords(xbillRecords)

    assert(expectedRecords == actualRecords)
  }

  behavior of "create"

  it should "return an object that extends DNSLookup" in {
    val actual = DNSLookupImpl.create().getClass
    val expected = classOf[DNSLookup]

    assert(expected.isAssignableFrom(actual))
  }
}
