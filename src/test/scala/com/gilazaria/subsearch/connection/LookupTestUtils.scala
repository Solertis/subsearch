package com.gilazaria.subsearch.connection

import java.net.InetAddress
import java.util.{Calendar, Date, GregorianCalendar, TimeZone}

import org.xbill.DNS
import org.xbill.DNS.{Name, TextParseException}

object LookupTestUtils {
  def createARecord(name: String, ip: String): DNS.ARecord =
    new DNS.ARecord(new DNS.Name(name), DNS.DClass.IN, 300, InetAddress.getByName(ip))

  def createNSRecord(name: String, target: String): DNS.NSRecord =
    new DNS.NSRecord(new DNS.Name(name), DNS.DClass.IN, 300, new DNS.Name(target))

  def createSOARecord(name: String, data: String): DNS.SOARecord = {
    val args = data.split(" ")
    new DNS.SOARecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      new DNS.Name(args(0)),
      new DNS.Name(args(1)),
      args(2).toInt,
      args(3).toInt,
      args(4).toInt,
      args(5).toInt,
      args(6).toInt)
  }

  def createRRSIGRecord(name: String, data: String): DNS.RRSIGRecord = {
    val args = data.split(" ")

    val signature: Array[Byte] = DNS.utils.base64.fromString(args.slice(8, args.size+1).mkString(""))

    new DNS.RRSIGRecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      args(1).toInt,
      args(2).toInt,
      args(3).toInt,
      dateFromFormattedTime(args(4)),
      dateFromFormattedTime(args(5)),
      args(6).toInt,
      new DNS.Name(args(7)),
      signature
    )

    // SOA 8 2 7200 20160330133700 20160229123700 44244 zonetransfer.me. GzQojkYAP8zuTOB9UAx66mTDiEGJ26hVIIP2ifk2DpbQLrEAPg4M77i4 M0yFWHpNfMJIuuJ8nMxQgFVCU3yTOeT/EMbN98FYC8lVYwEZeWHtbMmS 88jVlF+cOz2WarjCdyV0+UJCTdGtBJriIczC52EXKkw2RCkv3gtdKKVa fBE=
  }

  def createHINFORecord(name: String, cpu: String, os: String): DNS.HINFORecord =
    new DNS.HINFORecord(new Name(name), DNS.DClass.IN, 300, cpu, os)

  def createNSECRecord(name: String, data: String): DNS.NSECRecord = {
    val args = data.split(" ")
    val types: Array[Int] = args.slice(1, args.size+1).map(DNS.Type.value)

    new DNS.NSECRecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      new DNS.Name(args(0)),
      types
    )
  }

  def createDNSKEYRecord(name: String, data: String): DNS.DNSKEYRecord = {
    val args = data.split(" ")
    val key: Array[Byte] = DNS.utils.base64.fromString(args.slice(3, args.size+1).mkString(""))

    new DNS.DNSKEYRecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      args(0).toInt,
      args(1).toInt,
      args(2).toInt,
      key
    )
  }

  def createSRVRecord(name: String, data: String): DNS.SRVRecord = {
    val args = data.split(" ")

    new DNS.SRVRecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      args(0).toInt,
      args(1).toInt,
      args(2).toInt,
      new DNS.Name(args(3))
    )
  }

  def createPTRRecord(name: String, target: String): DNS.PTRRecord =
    new DNS.PTRRecord(
      new DNS.Name(name),
      DNS.DClass.IN,
      300,
      new DNS.Name(target)
    )

  def dateFromFormattedTime(s: String): Date = {
    if (s.length != 14) throw new TextParseException("Invalid time encoding: " + s)

    val c: Calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"))
    c.clear()

    try {
      val year: Int = s.substring(0, 4).toInt
      val month: Int = s.substring(4, 6).toInt - 1
      val date: Int = s.substring(6, 8).toInt
      val hour: Int = s.substring(8, 10).toInt
      val minute: Int = s.substring(10, 12).toInt
      val second: Int = s.substring(12, 14).toInt
      c.set(year, month, date, hour, minute, second)
    }
    catch {
      case e: NumberFormatException =>
        throw new TextParseException("Invalid time encoding: " + s)
    }

    c.getTime
  }
}
