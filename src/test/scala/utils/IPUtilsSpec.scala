package utils

import org.scalatest.{GivenWhenThen, FlatSpec}
import utils.IPUtils._

class IPUtilsSpec extends FlatSpec with GivenWhenThen {

  behavior of "normalise"

  it should "be the same" in {
    val ip: String = "127.0.0.1"
    Given(s"the ip has no whitespace: '$ip'")

    When("it is normalised")
    val actual = normalise(ip)

    Then("it shouldn't have changed")
    assert(ip == actual)
  }

  it should "trim whitespace" in {
    val ip: String = "  127.0.0.1  "
    Given(s"the ip has whitespace on either side: '$ip'")

    When("it is normalised")
    val actual = normalise(ip)

    Then("it should have no leading or trailing whitespace")
    val expected = "127.0.0.1"
    assert(actual == expected)
  }

  behavior of "isValidIPv4"

  it should "return true for '127.0.0.1'" in {
    val ip = "127.0.0.1"

    val actual = isValidIPv4(ip)
    val expected = true

    assert(actual == expected)
  }

  it should "return false for ' 127.0.0.1 '" in {
    val ip = " 127.0.0.1 "

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for quadrants that represent 0 as more than one 0 (e.g. 000)" in {
    val ip = "127.000.0.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for a number larger than 255 in the first quadrant" in {
    val ip = "900.0.0.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for a number larger than 255 in the second quadrant" in {
    val ip = "1.256.0.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for a number larger than 255 in the third quadrant" in {
    val ip = "1.0.256.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for a number larger than 255in the fourth quadrant" in {
    val ip = "1.0.0.256"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return true for a valid IP" in {
    val ip = "0.0.0.1"

    val actual = isValidIPv4(ip)
    val expected = true

    assert(actual == expected)
  }

  it should "return false for a non-zero quadrant number beginning with zero (e.g. 01)" in {
    val ip = "1.01.0.255"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for an ip with less than four quadrants" in {
    val ip = "127.0.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }

  it should "return false for an ip with more than four quadrants" in {
    val ip = "127.0.0.0.1"

    val actual = isValidIPv4(ip)
    val expected = false

    assert(actual == expected)
  }
}
