package utils

object IPUtils {
  def normalise(ip: String): String =
    ip.trim

  private val validRegexPattern = "^[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}$".r.pattern
  def isValid(resolver: String): Boolean =
    validRegexPattern.matcher(resolver).matches
}
