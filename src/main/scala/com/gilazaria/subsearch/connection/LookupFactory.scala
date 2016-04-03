package com.gilazaria.subsearch.connection

import org.xbill.DNS.{Record, SimpleResolver}

trait LookupFactory {
  def run(): Array[Record]
  def setResolver(resolver: SimpleResolver): Unit
  def getResult: Int
  def getAnswers: Array[Record]
}
