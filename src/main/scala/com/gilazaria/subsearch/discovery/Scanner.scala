package com.gilazaria.subsearch.discovery

import scala.concurrent.Future

trait Scanner {
  def scan(hostname: String): Future[Set[String]]
  val name: String
}
