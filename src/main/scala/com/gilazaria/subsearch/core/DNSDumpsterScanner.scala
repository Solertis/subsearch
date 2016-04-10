package com.gilazaria.subsearch.core

import com.gilazaria.subsearch.output.Logger
import com.ning.http.client.cookie.Cookie
import dispatch._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

class DNSDumpsterScanner private[core] (private val logger: Logger,
                                        private val browser: Browser = JsoupBrowser(),
                                        private val http: HttpExecutor = Http)(implicit ec: ExecutionContext) {
  import DNSDumpsterScanner.TokenNotFoundException

  private[core] val dnsDumpsterURL: String = "https://dnsdumpster.com/"

  def performScan(hostname: String): Future[Set[String]] = {
    logger.logDNSDumpsterScanStarted()

    retrieveCSRFToken()
      .flatMap(token => retrieveHTMLWithTokenForHostname(token, hostname))
      .map(html => extractSubdomains(html, hostname))
      .andThen { case subdomains => if (subdomains.isSuccess) logger.logDNSDumpsterFoundSubdomains(subdomains.get) }
      .recover(handleException)
      .andThen { case _ => logger.logDNSDumpsterScanCompleted() }
  }

  // Can also throw an IOException
  private[core] def retrieveCSRFToken(): Future[String] = {
    val promise: Promise[String] = Promise()

    Future {
      val documentAttempt = Try(browser.get(dnsDumpsterURL))

      if (documentAttempt.isFailure) {
        promise.failure(documentAttempt.failed.get)
      } else {
        val tokenInputList: List[Element] =
          (documentAttempt.get.body >> elementList("input"))
            .filter(_.attr("name") == "csrfmiddlewaretoken")

        if (tokenInputList.isEmpty) promise.failure(new TokenNotFoundException("No CSRF token was found."))
        else promise.success(tokenInputList.head.attr("value"))
      }
    }

    promise.future
  }

  private[core] def retrieveHTMLWithTokenForHostname(token: String, hostname: String): Future[String] = {
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

  private[core] def extractSubdomains(html: String, hostname: String): Set[String] = {
    (browser.parseString(html).body >> elementList(".col-md-4"))
      .map(e => e.innerHtml.split("<br>").head)
      .filter(subdomain => subdomain.endsWith(hostname) && subdomain != hostname)
      .toSet
  }

  private[core] val handleException: PartialFunction[Throwable, Set[String]] = {
    case cause: Throwable =>
      logger.logDNSDumpsterConnectionError(cause.getMessage)
      Set.empty
  }
}

object DNSDumpsterScanner {
  def create(logger: Logger)(implicit ec: ExecutionContext) =
    new DNSDumpsterScanner(logger)

  case class TokenNotFoundException(msg: String) extends Exception(msg)
}