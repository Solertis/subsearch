package com.gilazaria.subsearch.model

import org.xbill.DNS.Type

case class RecordType(stringValue: String) {
  override def toString = stringValue
  lazy val intValue: Int = Type.value(stringValue)
}

object RecordType {
  val A = RecordType("A")
  val AAAA = RecordType("AAAA")
  val ANY = RecordType("ANY")
  val AXFR = RecordType("AXFR")
  val CNAME = RecordType("CNAME")
  val MX = RecordType("MX")
  val NS = RecordType("NS")
}
