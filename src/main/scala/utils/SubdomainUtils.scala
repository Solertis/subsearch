package utils

import connection.Record

object SubdomainUtils {
  def normalise(hostname: String): String =
    hostname
      .trim // Trim leading/trailing whitespaces
      .stripPrefix(".").stripSuffix(".").trim // Trim leading/trailing dots
      .toLowerCase

  private val validDomainRegexPattern = "^[a-z0-9-_]+[.]([a-z0-9-_]+[.])*[a-z0-9-_]+$".r.pattern
  def isValidDomain(hostname: String): Boolean =
    validDomainRegexPattern.matcher(hostname).matches

  private val validSubdomainPartRegexPattern = "^[a-z0-9-_]+([.][a-z0-9-_]+)*$".r.pattern
  def isValidSubdomainPart(part: String): Boolean =
    validSubdomainPartRegexPattern.matcher(part).matches

  def ensureSubdomainEndsWithHostname(subdomain: String, hostname: String): String =
    if (subdomain.endsWith(hostname)) subdomain
    else s"$subdomain.$hostname"

  def recordTypesForSubdomainInRecords(subdomain: String, records: List[Record]): List[String] =
    records
      .filter(_.name == subdomain)
      .map(_.recordType)
      .distinct
      .sorted

  def distinctAndSortedNames(records: List[Record]): List[String] =
    records
      .map(_.name)
      .distinct
      .sorted

  def distinctAndSortedTypes(records: List[Record]): List[String] =
    records
      .map(_.recordType)
      .distinct
      .sorted
}
