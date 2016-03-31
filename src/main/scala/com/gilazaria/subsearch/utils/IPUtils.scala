package com.gilazaria.subsearch.utils

import scala.util.Try

object IPUtils {
  def normalise(ip: String): String =
    ip.trim

  def isValidIPv4(ip: String): Boolean = {
    val parts = ip.split("\\.", -1)

    parts.length == 4 &&
      parts
        .filter(string => Try(string.toInt).isSuccess) // It's an integer
        .filter(num => num.toInt != 0 || (num.toInt == 0 && num == "0")) // 0 is 0 and not 00 or 000 or ...
        .filter(num => !(num.toInt != 0 && num.startsWith("0"))) // 1 is not 01
        .map(_.toInt)
        .count(num => 0 <= num && num <= 255) == 4


  }
}
