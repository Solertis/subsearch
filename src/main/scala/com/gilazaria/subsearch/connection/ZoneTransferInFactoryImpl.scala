package com.gilazaria.subsearch.connection

import org.xbill.DNS._

class ZoneTransferInFactoryImpl(private val zoneTransferIn: ZoneTransferIn) extends ZoneTransferInFactory {
  def run(): java.util.List[_] = zoneTransferIn.run()
}

object ZoneTransferInFactoryImpl {
  def newAXFR(zone: Name, host: String, key: TSIG): ZoneTransferInFactory =
    new ZoneTransferInFactoryImpl(ZoneTransferIn.newAXFR(zone, host, key))
}
