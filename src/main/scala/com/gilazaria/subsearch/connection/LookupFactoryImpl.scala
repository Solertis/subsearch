package com.gilazaria.subsearch.connection

import org.xbill.DNS.{Lookup, Record, SimpleResolver}

class LookupFactoryImpl(hostname: String, recordType: Int) extends LookupFactory {
  private val lookup: Lookup = new Lookup(hostname, recordType)

  def run(): Array[Record] = lookup.run()
  def setResolver(resolver: SimpleResolver): Unit = lookup.setResolver(resolver)
  def getResult: Int = lookup.getResult
  def getAnswers: Array[Record] = lookup.getAnswers
}
