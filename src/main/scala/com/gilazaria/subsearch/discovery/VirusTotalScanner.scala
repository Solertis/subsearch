package com.gilazaria.subsearch.discovery

import dispatch.{Http, HttpExecutor, Req, url}
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, Future}

class VirusTotalScanner private[discovery] (private val browser: Browser = JsoupBrowser(),
                                            private val http: HttpExecutor = Http)
                                           (implicit ec: ExecutionContext)
extends Scanner {
  override val name: String = "Virus Total Scanner"

  override def scan(hostname: String): Future[Set[String]] = {
    retrieveHTML(hostname)
      .map(html => extractSubdomains(html, hostname))
  }

  private[discovery] def extractSubdomains(html: String, hostname: String): Set[String] =
    (browser.parseString(html).body >> elementList("a"))
      .map(e => e.innerHtml)
      .filter(subdomain => subdomain.endsWith(hostname) && subdomain != hostname)
      .toSet

  private[discovery] def retrieveHTML(hostname: String): Future[String] = {
    val request: Req =
      url(s"https://www.virustotal.com/en-gb/domain/$hostname/information/")
        .GET
        .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:45.0) Gecko/20100101 Firefox/45.0")
        .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .setHeader("Accept-Language", "en-US,en;q=0.5")

    http(request).map(_.getResponseBody)
  }
}

object VirusTotalScanner {
  def conditionallyCreate(create: Boolean)(implicit ec: ExecutionContext): Option[VirusTotalScanner] =
    if (create) Some(VirusTotalScanner.create())
    else None

  def create()(implicit ec: ExecutionContext): VirusTotalScanner =
    new VirusTotalScanner()
}