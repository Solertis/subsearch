package com.gilazaria.subsearch.connection

class Record private(val name: String, val recordType: String, val data: String)

object Record {
  def apply(name: String, recordType: String, data: String): Record = {
    val importantData =
      if (List("CNAME", "NS").contains(recordType))
        data.stripSuffix(".").trim
      else if (recordType == "SRV")
        data.split(" ")(3).stripSuffix(".").trim
      else if (recordType == "MX")
        data.split(" ")(1).stripSuffix(".").trim
      else
        data

    new Record(name.stripSuffix(".").trim, recordType, importantData)
  }

  def unapply(record: Record): Option[(String, String, String)] =
    Some((record.name, record.recordType, record.data))
}
