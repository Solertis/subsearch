package com.gilazaria.subsearch.utils

import com.gilazaria.subsearch.connection.Record
import org.scalatest.FlatSpec
import com.gilazaria.subsearch.utils.HostnameUtils._

class HostnameUtilsSpec extends FlatSpec {

  behavior of "normalise"

  it should "trim leading and trailing whitespace" in {
    val subdomain = " sub.domain.com "

    val expected = "sub.domain.com"
    val actual = normalise(subdomain)

    assert(expected == actual)
  }

  it should "trim leading and trailing dots" in {
    val subdomain = ".sub.domain.com."

    val expected = "sub.domain.com"
    val actual = normalise(subdomain)

    assert(expected == actual)
  }

  it should "convert to lowercase" in {
    val subdomain = "SuB.DoMaIn.com"

    val expected = "sub.domain.com"
    val actual = normalise(subdomain)

    assert(expected == actual)
  }

  behavior of "isValidDomain"

  List(
    ("sub.domain.com", true),
    ("domain.com", true),
    ("domain..com", false),
    ("sub..domain.com", false),
    ("a.b.c.d.e.f.g.h", true),
    (".a.com", false),
    ("a.com.", false),
    ("com", false),
    ("#.com", false),
    ("", false))
    .foreach {
      (data: (String, Boolean)) =>
        val domain = data._1
        val expected = data._2

        it should s"return $expected for $domain" in {
          val actual = isValidDomain(domain)
          assert(expected == actual)
        }
    }

  behavior of "isValidSubdomainPart"

  List(
    ("sub.domain.com", true),
    ("domain.com", true),
    ("domain..com", false),
    ("sub..domain.com", false),
    ("a.b.c.d.e.f.g.h", true),
    (".a.com", false),
    ("a.com.", false),
    ("#.com", false),
    ("", false),
    ("name", true))
    .foreach {
      (data: (String, Boolean)) =>
        val part = data._1
        val expected = data._2

        it should s"return $expected for $part" in {
          val actual = isValidSubdomainPart(part)
          assert(expected == actual)
        }
    }

  behavior of "ensureSubdomainEndsWithHostname"

  it should "append the hostname" in {
    val subdomain = "sub"
    val hostname = "domain.com"

    val expected = "sub.domain.com"
    val actual = ensureSubdomainEndsWithHostname(subdomain, hostname)

    assert(expected == actual)
  }

  it should "not append the hostname" in {
    val subdomain = "sub.domain.com"
    val hostname = "domain.com"

    val expected = "sub.domain.com"
    val actual = ensureSubdomainEndsWithHostname(subdomain, hostname)
  }

  behavior of "recordTypesForSubdomainInRecords"

  it should "return a distinct sorted list of record types for a specific subdomain" in {
    val subdomain = "aaaaa"
    val records = List(Record("A", "aaaaa", "bbbbb"), Record("CNAME", "asdas", "asda"), Record("NS", "aaaaa", "bbbbb"), Record("A", "ccccc", "ddddd"), Record("AAAA", "a", "b"))

    val expected = List("A", "NS")
    val actual = recordTypesForSubdomainInRecords(subdomain, records)

    assert(expected == actual)
  }

  behavior of "distinctAndSortedNames"

  it should "return a distinct sorted list of record names" in {
    val records = List(Record("A", "aaaaa", "bbbbb"), Record("CNAME", "asdas", "asda"), Record("NS", "aaaaa", "bbbbb"), Record("A", "ccccc", "ddddd"), Record("AAAA", "a", "b"))

    val expected = List("a", "aaaaa", "asdas", "ccccc")
    val actual = distinctAndSortedNames(records)

    assert(expected == actual)
  }

  behavior of "distinctAndSortedTypes"

  it should "return a distinct sorted list of record types" in {
    val records = List(Record("A", "aaaaa", "bbbbb"), Record("CNAME", "asdas", "asda"), Record("A", "ccccc", "ddddd"), Record("AAAA", "a", "b"))

    val expected = List("A", "AAAA", "CNAME")
    val actual = distinctAndSortedTypes(records)

    assert(expected == actual)
  }

}
