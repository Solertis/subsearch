package com.gilazaria.subsearch.connection

import java.net.InetAddress

import org.xbill.DNS

object LookupTestUtils {
  def createDNSARecord(name: String, ip: String): DNS.ARecord =
    new DNS.ARecord(new DNS.Name(name), DNS.DClass.IN, 300, InetAddress.getByName(ip))

  def createDNSNSRecord(name: String, target: String): DNS.NSRecord =
    new DNS.NSRecord(new DNS.Name(name), DNS.DClass.IN, 300, new DNS.Name(target))

  def createDNSSOARecord(name: String, data: String): DNS.SOARecord = {
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
}
