package com.gilazaria.subsearch.connection

import com.gilazaria.subsearch.model.{Record, RecordType}

import scala.util.Try

trait DNSLookupTrait {
  def performQueryOfTypeANY(hostname: String, resolver: String): Try[Set[Record]] =
    performQueryOfType(hostname, resolver, RecordType.ANY)

  def performQueryOfType(hostname: String, resolver: String, recordType: RecordType): Try[Set[Record]]
}
