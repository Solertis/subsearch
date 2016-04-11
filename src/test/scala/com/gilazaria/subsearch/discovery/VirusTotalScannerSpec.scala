package com.gilazaria.subsearch.discovery

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.concurrent.ExecutionContext.Implicits.global

class VirusTotalScannerSpec extends FlatSpec with MockFactory {

  behavior of "retrieveHTML"

  it should "return the HTML body" in {
    val hostname = "example.com"
  }

  behavior of "conditionallyCreate"

  it should "return a VirusTotalScanner" in {
    val scanner: Option[VirusTotalScanner] = VirusTotalScanner.conditionallyCreate(create = true)

    assert(scanner.isDefined)

    val actualClass = scanner.get.getClass
    val expectedClass = classOf[VirusTotalScanner]

    assert(expectedClass == actualClass)
  }

  it should "return None" in {
    val scanner: Option[VirusTotalScanner] = VirusTotalScanner.conditionallyCreate(create = false)

    assert(scanner.isEmpty)
  }

  behavior of "create"

  it should "return a VirusTotalScanner" in {
    val actualClass = VirusTotalScanner.create().getClass
    val expectedClass = classOf[VirusTotalScanner]

    assert(expectedClass == actualClass)
  }
}
