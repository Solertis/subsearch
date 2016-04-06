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

class ZoneTransferScannerSpec extends FlatSpec with MockFactory {

  val mockLogger: Logger = mock[Logger]
  val mockLookup: DNSLookup = mock[DNSLookup]
  val mockExecutionContext: ExecutionContext = mock[ExecutionContext]

  behavior of "ZoneTransferScannerSpec"

  it should "" in {

  }

  it should "recordsEndingWithHostname" in {

  }

  it should "zoneTransferForHostnameAndResolver" in {

  }

  behavior of "zoneTransferForHostnameAndResolver"

  it should "return the correct records and then log that the resolver is vulnerable" in {
    val resolver = "1.1.1.1"

    val hostname = "zonetransfer.me."
    val siphostname = s"_sip._tcp.$hostname"
    val arpahostname = s"157.177.147.217.IN-ADDR.ARPA.$hostname"
    val wwwhostname = s"www.$hostname"

    val records =
      SortedSet(
        Record(hostname, RecordType.SOA, "nsztm1.digi.ninja. robin.digi.ninja. 2014101603 172800 900 1209600 3600"),
        Record(hostname, RecordType.RRSIG, "SOA 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. GzQojkYAP8zuTOB9UAx66mTDiEGJ26hVIIP2ifk2DpbQLrEAPg4M77i4 M0yFWHpNfMJIuuJ8nMxQgFVCU3yTOeT/EMbN98FYC8lVYwEZeWHtbMmS 88jVlF+cOz2WarjCdyV0+UJCTdGtBJriIczC52EXKkw2RCkv3gtdKKVa fBE="),
        Record(hostname, RecordType.RRSIG, "NS 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. TyFngBk2PMWxgJc6RtgCE/RhE0kqeWfwhYSBxFxezupFLeiDjHeVXo+S WZxP54Xvwfk7jlFClNZ9lRNkL5qHyxRElhlH1JJI1hjvod0fycqLqCnx XIqkOzUCkm2Mxr8OcGf2jVNDUcLPDO5XjHgOXCK9tRbVVKIpB92f4Qal ulw="),
        Record(hostname, RecordType.RRSIG, "A 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. unoMaEPiyoAr0yAWg/coPbAFNznaAlUJW3/QrvJleer50VvGLW/cK+VE DcZLfCu6paQhgJHVddG4p145vVQe3QRvp7EJpUh+SU7dX0I3gngmOa4H k190S4utcXY5FhaN7xBKHVWBlavQaSHTg61g/iuLSB0lS1gp/DAMUpC+ WzE="),
        Record(hostname, RecordType.RRSIG, "HINFO 8 2 300 20160330133700 20160229123700 44244 zonetransfer.me. Xebvrpv8nCGn/+iHqok1rcItTPqcskV6jpJ1pCo4WYbnqByLultzygWx JlyVzz+wJHEqRQYDjqGblOdyUgKn2FFnqb1O92kKghcHHvoMEh+Jf5i7 0trtucpRs3AtlneLj2vauOCIEdbjma4IxgdwPahKIhgtgWcUInVFh3Rr SwM="),
        Record(hostname, RecordType.RRSIG, "NSEC 8 2 3600 20160330133700 20160229123700 44244 zonetransfer.me. MFHBRiIpk14ys3NIb81eCIl8rWULNDAokMeVUlXpwO9OF8jSuFOgPoHy zdAd5NdKe5O/QtuBFizLBTPoR41G5PzQwOZf3cwLEr+35cGsvujwu7Yz H2YU+bkNR/ZvOtYX8hMlM2WxivGyEM+ebZNVn0XcrOI3Kpi31VQ3SGTS Gcs="),
        Record(hostname, RecordType.RRSIG, "DNSKEY 8 2 300 20160330133700 20160229123700 44244 zonetransfer.me. LWKb719rS66s4b3Th1hCqN1dYsKM2hNTlSoHht7KSngj+zPbXckRQmEQ Dczk0DQO71mnxr8/V3iIHUwlEz54oVJZe/3VvNByL3Hdz88vJfXVjDl2 iPSZV9H+NMvocoWSa+63PinRXdfaSpt5wfd8PtRrO0cy9AZQfZc5Nvzx bvM="),
        Record(hostname, RecordType.RRSIG, "DNSKEY 8 2 300 20160330133700 20160229123700 47530 zonetransfer.me. zrmeIf1WdtuwrZgeJRgnP6SOLC+MXiw4UKUEvdA8bhdw1uHEAV8onMWO xQgHWOXV3/npzGhwTcLMoO6NqrV8dZyjL7hgo9PafECNWRIJmtEVOLhd uCiqd5fWFWabiqMr5fHkYIDocnjoz5iyau2ARhIun4U/sygKOPkVTAzP UhvywrNAp3vS+pSCTpPhybXuyU46dIKkZ0XAAwprfR4zT1utsSeafsK8 LGmf4RkmKZXO8nXiz2LKcBbrHv1JM9OvuNTYukI15RSyMDFRhfRCn+ZC UG12rJvWOg5zViaIK/phI5nNd281ghWvFs6Xb9/MJ3f3hWopDOk/ALel brPQ7g=="),
        Record(hostname, RecordType.RRSIG, "SRV 8 4 14000 20160330133700 20160229123700 44244 zonetransfer.me. trlrzT1EGDJ+I0GwCcCS1FU9HDiR+y9NOV3g+tyygeOUQDIE4Yr/UTvT e/UEjSEbluMOwAAPuMIFV9amchsbTBLIzxtVsZgPscJMB8vIA4UVSFOq NsuPbFd1uBsPJ9gJYjf7/L8hnZ3r6KDXxdOzHWhs/RhMKV9/pV+UMBzT fm0="),
        Record(hostname, RecordType.RRSIG, "NSEC 8 4 3600 20160330133700 20160229123700 44244 zonetransfer.me. zxoMlL21uMJDNv54lggFbbp13q001YAqXobmIGBKyJR7fjD5vLNs54s7 nOLbTMee9rjzz6fMLUrNscPKh+Io9K8vSLhGK/SQbip5d/uHkITj0OW1 10QZ800ZbM5apzxVEg60expe7PmcbxlunEqGLr9+aO377DSADkDagXhB oQA="),
        Record(hostname, RecordType.RRSIG, "PTR 8 8 7200 20160330133700 20160229123700 44244 zonetransfer.me. NLvoxfZKnVcap87fQs52MaUVX3Yi0pyQCrPJDkNNDIWvB78NHy6eNSZt 4cIb/oUgQW8Gw6/soXrMXBwJcG6gaVNXPEph1v8Yiobm3fyetGgeCSB8 rnhQwfCltiW1zCpIvkDZKIy7FgTLOSr+RjnPZA/cMu4c9QsJLgYXyytL hWs="),
        Record(hostname, RecordType.RRSIG, "NSEC 8 8 3600 20160330133700 20160229123700 44244 zonetransfer.me. jU2Gm0tnY2m+JF8q2ca7U03I6HWSsCzGGQoYEzla+7QebAs9ho66PpAQ RdmPKX5KNIYZJSop+492a/1CBT/WR0hqUAgVS/UOuutCyrHqpeVWnl7x tx5CujVs1+Mn48RlGBzanTDzhRr44h/4nNJtZM3/OTWBkpnpakBWn/W6 EdI="),
        Record(siphostname, RecordType.RRSIG, "A 8 3 7200 20160330133700 20160229123700 44244 zonetransfer.me. tVFIQ2rklljhe64SM8JAn740XSk+MIEXuJJQ9u6Qzmti058hA0hEzdrz 7nAt/LmMcC9RCFTAOl8CSewVExhHgiwu1sO5i69jpI0G87eecAYnGnJe Cd9pf0Wfvm+3ucIxOKm59YmHXOF/48dJlyoKnXNEg6nCBzoW9aq3OreO 16s="),
        Record(siphostname, RecordType.RRSIG, "NSEC 8 3 3600 20160330133700 20160229123700 44244 zonetransfer.me. 0xCqc6tWcT11ACD24Ap68hc7HRyAcCf7MrkDqe2HyYMGuGS9YSwosiF3 QzffhuY5qagIFbpI3f7xVGxykngThTk37/JO2SrfI7Z5kvqLHdEd6GD9 sogsLqTfHE9UToOYYfuasO+IsJLyPALh89yk3bY+NipvpEPngSnxN6eh Ikc="),
        Record(hostname, RecordType.NS, "nsztm1.digi.ninja."),
        Record(hostname, RecordType.NS, "nsztm2.digi.ninja."),
        Record(siphostname, RecordType.A, "217.147.177.157"),
        Record(siphostname, RecordType.HINFO, "\"Casio fx-700G\" \"Windows XP\""),
        Record(arpahostname, RecordType.NSEC, "_sip._tcp.zonetransfer.me. A NS SOA HINFO RRSIG NSEC DNSKEY"),
        Record(arpahostname, RecordType.NSEC, "157.177.147.217.IN-ADDR.ARPA.zonetransfer.me. SRV RRSIG NSEC"),
        Record(arpahostname, RecordType.NSEC, "asfdbauthdns.zonetransfer.me. PTR RRSIG NSEC"),
        Record(arpahostname, RecordType.DNSKEY, "256 3 8 AwEAAdXeqhjJnKHczUifC98Nz2xg2xM6DXe0JEE3dNenGNyoByUrxG2m QoZy78l2Pshjc/gKwcTjawHKeI8/aO16xP9nG4pgKyybLPJN9XvJRV5t XFok48g2DPbeu0/TPNoVYALrMeruqxFGv/a/RijzKxrdE3Eqle1906zg nXf43hkL"),
        Record(wwwhostname, RecordType.DNSKEY, "256 3 8 AwEAAdd50fHa9wVKIf9/gpOhFx3NBq32WGq/SykRNUwSMnPU3OOkRzUT KEL+yxdoARvL/pbR+5pK3WAOVzsLKdy3+xeLqfILdgY1P0msjVXXNN2o mBV+Hs2Lip7qS4RkYJHg+Oq8RWixpeCSAJ1/DkSLE20HYYiq27a07pNE oc+OfNef"),
        Record(wwwhostname, RecordType.DNSKEY, "257 3 8 AwEAAeFLORXsixEH2ftm5oo4VQI66D6zMBreinysTdx5jOVgdAtOa3I6 L3ye7lXZFJbdnPZw0w0df2NvWbIHDzICQxbJjXTUkWuN0wuXm9f6BFP8 VcNa8nPm14F2+6ue+cMxyMOXZW/4vt2DV5hsH+lNWQxGVeTFdCifUlYt sPloxspYETT5vhquKj/VNariRecX0zTzyM67F+grCR+cUo75CnZaRbVZ G7gIBQ783tVO5tFQCUJjqw3zO1f6eQp/xk4rNorCK7BDqitgUbpdPuFh CqmcKVYsFrEViNbaI/Nk9lJHH+nQaYBSZx+cWrTLPZtkNp3ahqY3gLwA 0RUgfYZCWnU="),
        Record(wwwhostname, RecordType.SRV, "0 0 5060 www.zonetransfer.me."),
        Record(wwwhostname, RecordType.A, "217.147.177.157")
      )

    (mockLookup.performQueryOfType _).expects(hostname, resolver, RecordType.AXFR).returns(Try(records))
    (mockLogger.logNameServerVulnerableToZoneTransfer _).expects(resolver)

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new ZoneTransferScanner(mockLogger, mockLookup)

    val expectedRecords = records
    val actualRecords = Await.result(scanner.zoneTransferForHostnameAndResolver(hostname, resolver), TimeUtils.awaitDuration)

    assert(expectedRecords == actualRecords)
  }

  it should "return no records and then log that the resolver is vulnerable" in {
    val hostname = "domain.com"
    val resolver = "1.1.1.1"

    val records = SortedSet.empty[Record]

    (mockLookup.performQueryOfType _).expects(hostname, resolver, RecordType.AXFR).returns(Try(records))

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new ZoneTransferScanner(mockLogger, mockLookup)

    val expectedRecords = records
    val actualRecords = Await.result(scanner.zoneTransferForHostnameAndResolver(hostname, resolver), TimeUtils.awaitDuration)

    assert(expectedRecords == actualRecords)
  }

  behavior of "flattenRecords"

  it should "return an empty set #1" in {
    val set: Set[SortedSet[Record]] = Set.empty

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  it should "return an empty set #2" in {
    val set: Set[SortedSet[Record]] = Set(SortedSet.empty[Record])

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  it should "return an empty set #3" in {
    val set: Set[SortedSet[Record]] = Set(SortedSet.empty[Record], SortedSet.empty[Record])

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  it should "return a combined set #1" in {
    val set: Set[SortedSet[Record]] =
      Set(
        SortedSet(Record("domain.com.", RecordType.A, "1.1.1.1")),
        SortedSet(Record("domain.com.", RecordType.A, "2.2.2.2"))
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2")
      )
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  it should "return a combined set #2" in {
    val set: Set[SortedSet[Record]] =
      Set(
        SortedSet(Record("domain.com.", RecordType.A, "1.1.1.1")),
        SortedSet(
          Record("domain.com.", RecordType.A, "2.2.2.2"),
          Record("example.com.", RecordType.CNAME, "sub.example.com.")
        )
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2"),
        Record("example.com.", RecordType.CNAME, "sub.example.com.")
      )
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  it should "return a combined set #3" in {
    val set: Set[SortedSet[Record]] =
      Set(
        SortedSet(
          Record("domain.com.", RecordType.A, "1.1.1.1"),
          Record("example.com.", RecordType.CNAME, "sub.example.com.")
        ),
        SortedSet(
          Record("domain.com.", RecordType.A, "2.2.2.2"),
          Record("example.com.", RecordType.CNAME, "sub.example.com.")
        )
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2"),
        Record("example.com.", RecordType.CNAME, "sub.example.com.")
      )
    val actualRecords = scanner.flattenRecords(set)

    assert (expectedRecords == actualRecords)
  }

  behavior of "recordsEndingWithHostname"

  it should "return empty" in {
    val hostname = ""
    val records = SortedSet.empty[Record]

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = records
    val actualRecords = scanner.recordsEndingWithHostname(hostname, records)

    assert(expectedRecords == actualRecords)
  }

  it should "only return records ending with domain.com" in {
    val hostname = "domain.com"
    val records =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2"),
        Record("example.com.", RecordType.CNAME, "sub.example.com.")
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2")
      )
    val actualRecords = scanner.recordsEndingWithHostname(hostname, records)

    assert(expectedRecords == actualRecords)
  }

  it should "return no records" in {
    val hostname = "example.com"
    val records =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2")
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = SortedSet.empty[Record]
    val actualRecords = scanner.recordsEndingWithHostname(hostname, records)

    assert(expectedRecords == actualRecords)
  }

  behavior of "printFoundRecords"

  it should "tell the logger to print records, that the transfer has completed and return records" in {
    val records =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2"),
        Record("sub.domain.com.", RecordType.CNAME, "sub2.domain.com.")
      )

    (mockLogger.logRecords _).expects(records)
    (mockLogger.logZoneTransferCompleted _).expects()

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = records
    val actualRecords = scanner.printFoundRecords(records)

    assert(expectedRecords == actualRecords)
  }

  it should "tell the logger that no records were found, that the transfer has compelted and return records" in {
    val records = SortedSet.empty[Record]

    (mockLogger.logNameServersNotVulnerableToZoneTransfer _).expects()
    (mockLogger.logZoneTransferCompleted _).expects()

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedRecords = records
    val actualRecords = scanner.printFoundRecords(records)

    assert(expectedRecords == actualRecords)
  }

  behavior of "namesFromRecords"

  it should "return the names from each Record" in {
    val records =
      SortedSet(
        Record("domain.com.", RecordType.A, "1.1.1.1"),
        Record("domain.com.", RecordType.A, "2.2.2.2"),
        Record("sub.domain.com.", RecordType.CNAME, "sub2.domain.com.")
      )

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedNames = records.map(_.name).toSet
    val actualNames = scanner.namesFromRecords(records)

    assert(expectedNames == actualNames)
  }

  it should "return empty" in {
    val records = SortedSet.empty[Record]

    val scanner = ZoneTransferScanner.create(mockLogger)(mockExecutionContext)

    val expectedNames = Set.empty
    val actualNames = scanner.namesFromRecords(records)

    assert(expectedNames == actualNames)
  }

  behavior of "create"

  it should "return a ZoneTransferScanner" in {
    val actualClass = ZoneTransferScanner.create(mockLogger)(mockExecutionContext).getClass
    val expectedClass = classOf[ZoneTransferScanner]

    assert(expectedClass == actualClass)
  }

}
