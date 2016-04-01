package com.gilazaria.subsearch.utils

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

}
