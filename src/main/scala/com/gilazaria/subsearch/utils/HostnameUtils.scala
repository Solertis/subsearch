package com.gilazaria.subsearch.utils

import com.gilazaria.subsearch.model.Record

object HostnameUtils {
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
}
