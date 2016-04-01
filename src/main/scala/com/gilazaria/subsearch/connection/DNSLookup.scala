package com.gilazaria.subsearch.connection

import com.gilazaria.subsearch.model.{Record, RecordType}

import scala.collection.SortedSet
import scala.util.Try

trait DNSLookup {
  def performQueryOfTypeANY(hostname: String, resolver: String): Try[SortedSet[Record]] =
    performQueryOfType(hostname, resolver, RecordType.ANY)

  def performQueryOfType(hostname: String, resolver: String, recordType: RecordType): Try[SortedSet[Record]]
}
