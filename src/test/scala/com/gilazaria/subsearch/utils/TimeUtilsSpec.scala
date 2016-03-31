package com.gilazaria.subsearch.utils

import org.scalatest.FlatSpec
import com.gilazaria.subsearch.utils.TimeUtils._
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

class TimeUtilsSpec extends FlatSpec {

  // How can this be done?
  behavior of "currentTimePretty"

//  it should "currentTimePretty" in {
//
//  }

  behavior of "akkaAskTimeout"

  it should "have a finite duration of 21474835 seconds" in {
    val expected = FiniteDuration(21474835, "seconds")
    val actual = akkaAskTimeout.duration

    assert(expected == actual)
  }

  behavior of "awaitDuration"

  it should "be equal to 365 days" in {
    val expected = 365.days
    val actual = awaitDuration

    assert(expected == actual)
  }

  // How can this be done?
  behavior of "timestampNow"

//  it should "" in {
//
//  }

}
