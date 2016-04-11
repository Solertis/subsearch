package com.gilazaria.subsearch.discovery

import com.ning.http.client.cookie.Cookie
import dispatch._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

import scala.concurrent.{ExecutionContext, Future}

class DNSDumpsterScanner private[discovery] (private val browser: Browser = JsoupBrowser(),
                                             private val http: HttpExecutor = Http)
                                            (implicit ec: ExecutionContext)
extends Scanner {
  override val name: String = "DNS Dumpster Scanner"

  override def scan(hostname: String): Future[Set[String]] =
    retrieveCSRFToken()
      .flatMap(token => retrieveHTMLWithTokenForHostname(token, hostname))
      .map(html => extractSubdomains(html, hostname))

  private[discovery] val dnsDumpsterURL: String = "https://dnsdumpster.com/"

  private[discovery] def retrieveCSRFToken(): Future[String] =
    Future(browser.get(dnsDumpsterURL))
      .map {
        document =>
          val tokenInputList: List[Element] =
            (document.body >> elementList("input"))
              .filter(_.attr("name") == "csrfmiddlewaretoken")

          if (tokenInputList.isEmpty) throw new DNSDumpsterScanner.TokenNotFoundException("No CSRF token was found.")
          else tokenInputList.head.attr("value")
      }

  private[discovery] def retrieveHTMLWithTokenForHostname(token: String, hostname: String): Future[String] = {
    val request: Req =
      url(dnsDumpsterURL)
        .POST
        .setHeader("referer", dnsDumpsterURL)
        .setHeader("origin", dnsDumpsterURL)
        .addCookie(new Cookie("csrftoken", token, token, "dnsdumpster.com", "/", -1, -1, false, false))
        .addParameter("csrfmiddlewaretoken", token)
        .addParameter("targetip", hostname)

    http(request).map(_.getResponseBody)
  }

  private[discovery] def extractSubdomains(html: String, hostname: String): Set[String] =
    (browser.parseString(html).body >> elementList(".col-md-4"))
      .map(e => e.innerHtml.split("<br>").head)
      .filter(subdomain => subdomain.endsWith(hostname) && subdomain != hostname)
      .toSet
}

object DNSDumpsterScanner {
  def conditionallyCreate(create: Boolean)(implicit ec: ExecutionContext): Option[DNSDumpsterScanner] =
    if (create) Some(DNSDumpsterScanner.create())
    else None

  def create()(implicit ec: ExecutionContext): DNSDumpsterScanner =
    new DNSDumpsterScanner()

  case class TokenNotFoundException(msg: String) extends Exception(msg)
}