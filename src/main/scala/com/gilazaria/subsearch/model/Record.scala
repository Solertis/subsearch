package com.gilazaria.subsearch.model

class Record private(val name: String, val recordType: String, val data: String) extends Ordered[Record] {
  override def compare(that: Record): Int = {
    val compareName = compareString(this.name, that.name)
    val compareType = compareString(this.recordType, that.recordType)
    val compareData = compareString(this.data, that.data)

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
