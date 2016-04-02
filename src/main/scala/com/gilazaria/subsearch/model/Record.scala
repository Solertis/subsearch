package com.gilazaria.subsearch.model

import com.gilazaria.subsearch.utils.HostnameUtils

class Record private(val name: String, val recordType: RecordType, val data: String) extends Ordered[Record] {
  override def compare(that: Record): Int = {
    val compareName = compareString(this.name, that.name)
    lazy val compareType = recordType.compare(that.recordType)
    lazy val compareData = compareString(this.data, that.data)

    if (compareName != 0) compareName
    else if (compareType != 0) compareType
    else compareData
  }

  private def compareString(a: String, b: String): Int =
    if (a == b) 0
    else if (a > b) 1
    else -1
}

object Record {
  def apply(name: String, recordType: RecordType, data: String): Record = {
    val transformedName = HostnameUtils.normalise(name)

    val transformedData =
      if (recordType.isOneOf("CNAME", "NS")) HostnameUtils.normalise(data)
      else if (recordType.stringValue == "SRV") HostnameUtils.normalise(data.split(" ")(3))
      else if (recordType.stringValue == "MX") HostnameUtils.normalise(data.split(" ")(1))
      else data

    new Record(transformedName, recordType, transformedData)
  }

  def unapply(record: Record): Option[(String, RecordType, String)] =
    Some((record.name, record.recordType, record.data))
}
