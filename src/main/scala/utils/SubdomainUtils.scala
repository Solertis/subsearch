package utils

object SubdomainUtils {
  def normalise(subdomain: String): String =
    subdomain
      .trim // Trim leading/trailing whitespaces
      .stripPrefix(".").stripSuffix(".").trim // Trim leading/trailing dots
      .toLowerCase

  private val validRegexPattern = "([a-z0-9-_]|[.])*".r.pattern
  def isValid(subdomain: String): Boolean =
    validRegexPattern.matcher(subdomain).matches

  def ensureSubdomainEndsWithHostname(subdomain: String, hostname: String): String =
    if (subdomain.endsWith(hostname)) subdomain
    else s"$subdomain.$hostname"
}
