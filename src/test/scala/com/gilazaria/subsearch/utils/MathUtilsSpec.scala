package com.gilazaria.subsearch.utils

import org.scalatest.{GivenWhenThen, FlatSpec}
import com.gilazaria.subsearch.utils.MathUtils._

class MathUtilsSpec extends FlatSpec with GivenWhenThen {

  behavior of "percentage"

  it should "calculate 50.0" in {
    Given("a = 1 and b = 2")
    val a = 1
    val b = 2

    When("the percentage is calculated")
    val expected: Float = 50.0.toFloat
    val actual: Float = percentage(a, b)

    Then(s"the result should be $expected")
    assert(expected == actual)
  }

  it should "calculate 150.0 when given 3 and 2" in {
    Given("a = 3 and b = 2")
    val a = 3
    val b = 2

    When("the percentage is calculated")
    val expected: Float = 150.0.toFloat
    val actual: Float = percentage(a, b)

    Then(s"the result should be $expected")
    assert(expected == actual)
  }

  it should "throw an IllegalArgumentException" in {
    Given("b = 0")
    val a = 1
    val b = 0

    When("the percentage is calculated")
    val expectedException = intercept[IllegalArgumentException] {
      percentage(a, b)
    }

    Then("an IllegalArgumentException is thrown")
    assert(expectedException.getMessage === "The second argument cannot be zero.")
  }
}
