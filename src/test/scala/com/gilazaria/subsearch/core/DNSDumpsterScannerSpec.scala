package com.gilazaria.subsearch.core

import java.io.{IOException, InputStream}

import com.gilazaria.subsearch.core.DNSDumpsterScanner.TokenNotFoundException
import com.gilazaria.subsearch.output.Logger
import com.gilazaria.subsearch.utils.TimeUtils
import com.ning.http.client.Response
import dispatch.{HttpExecutor, Req}
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class DNSDumpsterScannerSpec extends FlatSpec with MockFactory {
  val exampleHTMLWithToken = "<!doctype html>\n<html lang=\"en\">\n <head> \n  <meta charset=\"utf-8\"> \n  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"> \n  <meta name=\"google-site-verification\" content=\"vAWNZCy-5XAPGRgA2_NY5HictfnByvgpqOLQUAmVZW0\"> \n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> \n  <meta name=\"description\" content=\"Find dns records in order to identify the Internet footprint of an organization. Recon that enables deeper security assessments and understanding of the potential attack surface.\"> \n  <meta name=\"author\" content=\"\"> \n  <link rel=\"icon\" href=\"/static/favicon.ico\"> \n  <title>DNSdumpster.com - dns recon and research, find and lookup dns records</title> \n  <!-- Bootstrap core CSS --> \n  <link href=\"/static/css/bootstrap.min.css\" rel=\"stylesheet\"> \n  <!-- Custom styles for this template --> \n  <link href=\"/static/cover.css\" rel=\"stylesheet\"> \n </head> \n <body> \n  <div class=\"site-wrapper\"> \n   <div class=\"site-wrapper-inner\"> \n    <!-- Section 1 --> \n    <section id=\"intro\" data-speed=\"6\" data-type=\"background\"> \n     <div class=\"container\"> \n      <div class=\"cover-container\"> \n       <div class=\"inner cover\"> \n        <h1 class=\"cover-heading\">dns recon &amp; research, find &amp; lookup dns records</h1> \n        <p class=\"lead\"> </p>\n        <div id=\"hideform\"> \n         <form role=\"form\" action=\".\" method=\"post\">\n          <input type=\"hidden\" name=\"csrfmiddlewaretoken\" value=\"BkgQgkNSuTrAWqLOYjk1z37GDh8vJbwK\"> \n          <div class=\"form-group\"> \n           <div class=\"col-md-2\"></div>\n           <div class=\"col-md-6\"> \n            <input class=\"form-control\" type=\"text\" placeholder=\"exampledomain.com\" name=\"targetip\" id=\"regularInput\"> \n           </div>\n          </div> \n          <div align=\"left\" id=\"formsubmit\">\n           <button type=\"submit\" class=\"btn btn-default\">Search <span class=\"glyphicon glyphicon-chevron-right\"></span></button>\n          </div> \n         </form>\n        </div>\n       </div> \n       <div class=\"row\">\n        <div class=\"col-md-2\"></div>\n        <div class=\"col-md-8\"> \n         <div id=\"showloading\" style=\"color: #fff;\">\n          Loading...\n          <br> \n          <div class=\"progress\"> \n           <div class=\"progress-bar progress-bar-success progress-bar-striped active\" role=\"progressbar\" aria-valuenow=\"45\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width: 100%\"> \n           </div>\n          </div>\n         </div>\n        </div>\n       </div> \n       <p></p> \n       <p class=\"lead\" style=\"margin-top: 40px; margin-bottom: 30px;\">DNSdumpster.com is a FREE domain research tool that can discover hosts related to a domain. Finding visible hosts from the attackers perspective is an important part of the security assessment process.</p> \n      </div> \n      <p style=\"color: #777;\">this is a <a href=\"https://hackertarget.com/\" title=\"Online Vulnerability Scanners\"><button type=\"button\" class=\"btn btn-danger btn-xs\">HackerTarget.com</button></a> project</p> \n      <p><a href=\"#section2\"><i style=\"background-color: #333; color: #ccc; font-size: 3em; line-height: 5em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p> \n     </div> \n    </section> \n    <!-- Section 2 --> \n    <section id=\"home\" data-speed=\"4\" data-type=\"background\"> \n     <a name=\"section2\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-trash\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.7em; line-height: 1.9em; margin-bottom: 80px;\">Map an organizations attack surface with a virtual <i>dumpster dive*</i> of the DNS records associated with the target organization.</p> \n       <p style=\"font-size: 1.2em; color: #ccc;\">*DUMPSTER DIVING: The practice of sifting refuse from an office or technical installation to extract confidential data, especially security-compromising information.<br><span style=\"font-size: 0.9;\"><i>Dictionary.com</i></span></p> \n       <p><a href=\"#section3\"><i style=\"background-color: #333; color: #ccc; font-size: 2em; line-height: 4em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p>  \n      </div> \n     </div>\n    </section> \n    <!-- Section 3 --> \n    <section id=\"about\" data-speed=\"2\" data-type=\"background\"> \n     <a name=\"section3\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-cog\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.6em; line-height: 1.7em; margin-bottom: 80px;\">More than a simple <a href=\"https://hackertarget.com/dns-lookup/\" title=\"Online DNS Lookup\">DNS lookup</a> this tool will discover those hard to find sub-domains and web hosts. No brute force of common sub-domains is undertaken as is common practice for many DNS recon tools. The search relies on data from search engines and the excellent <a href=\"https://scans.io\" style=\"text-decoration: underline;\">scans.io</a> research project.</p> \n       <p><a href=\"#section4\"><i style=\"background-color: #333; color: #ccc; font-size: 2em; line-height: 4em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p>  \n      </div> \n     </div>\n    </section> \n    <!-- Section 4 --> \n    <section id=\"about\" data-speed=\"2\" data-type=\"background\"> \n     <a name=\"section4\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-envelope\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.6em; line-height: 1.7em; margin-bottom: 80px;\">Receive updates by following <a href=\"https://twitter.com/hackertarget\" title=\"Twitter\" style=\"text-decoration: underline;\">@hackertarget</a> on Twitter<br> or subscribe to the low volume <a style=\"text-decoration: underline;\" href=\"http://eepurl.com/jDaVL\">mailing list</a>.</p>  \n       <p style=\"color: #777;\">this is a <a href=\"https://hackertarget.com/\" title=\"Online Vulnerability Scanners\"><button type=\"button\" class=\"btn btn-danger btn-xs\">HackerTarget.com</button></a> project</p>\n      </div> \n     </div>\n    </section> \n   </div> \n  </div> \n  <!-- Bootstrap core JavaScript\n    ================================================== --> \n  <!-- Placed at the end of the document so the pages load faster --> \n  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script> \n  <script src=\"/static/js/bootstrap.min.js\"></script> \n  <script src=\"/static/js/docs.min.js\"></script> \n  <!-- IE10 viewport hack for Surface/desktop Windows 8 bug --> \n  <script src=\"/static/js/ie10-viewport-bug-workaround.js\"></script> \n  <script>\n$(document).ready(function(){\n  $(\"#showloading\").hide();\n  $(\"#formsubmit\").click(function(){\n    $(\"#hideform\").hide();\n    $(\"#showloading\").show();\n  });\n\n});\n</script> \n  <script>\n  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n\n  ga('create', 'UA-2487671-6', 'auto');\n  ga('send', 'pageview');\n\n</script>   \n </body>\n</html>"
  val exampleHTMLWithoutToken = "<!doctype html>\n<html lang=\"en\">\n <head> \n  <meta charset=\"utf-8\"> \n  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"> \n  <meta name=\"google-site-verification\" content=\"vAWNZCy-5XAPGRgA2_NY5HictfnByvgpqOLQUAmVZW0\"> \n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> \n  <meta name=\"description\" content=\"Find dns records in order to identify the Internet footprint of an organization. Recon that enables deeper security assessments and understanding of the potential attack surface.\"> \n  <meta name=\"author\" content=\"\"> \n  <link rel=\"icon\" href=\"/static/favicon.ico\"> \n  <title>DNSdumpster.com - dns recon and research, find and lookup dns records</title> \n  <!-- Bootstrap core CSS --> \n  <link href=\"/static/css/bootstrap.min.css\" rel=\"stylesheet\"> \n  <!-- Custom styles for this template --> \n  <link href=\"/static/cover.css\" rel=\"stylesheet\"> \n </head> \n <body> \n  <div class=\"site-wrapper\"> \n   <div class=\"site-wrapper-inner\"> \n    <!-- Section 1 --> \n    <section id=\"intro\" data-speed=\"6\" data-type=\"background\"> \n     <div class=\"container\"> \n      <div class=\"cover-container\"> \n       <div class=\"inner cover\"> \n        <h1 class=\"cover-heading\">dns recon &amp; research, find &amp; lookup dns records</h1> \n        <p class=\"lead\"> </p>\n        <div id=\"hideform\"> \n         <form role=\"form\" action=\".\" method=\"post\">\n          <div class=\"form-group\"> \n           <div class=\"col-md-2\"></div>\n           <div class=\"col-md-6\"> \n            <input class=\"form-control\" type=\"text\" placeholder=\"exampledomain.com\" name=\"targetip\" id=\"regularInput\"> \n           </div>\n          </div> \n          <div align=\"left\" id=\"formsubmit\">\n           <button type=\"submit\" class=\"btn btn-default\">Search <span class=\"glyphicon glyphicon-chevron-right\"></span></button>\n          </div> \n         </form>\n        </div>\n       </div> \n       <div class=\"row\">\n        <div class=\"col-md-2\"></div>\n        <div class=\"col-md-8\"> \n         <div id=\"showloading\" style=\"color: #fff;\">\n          Loading...\n          <br> \n          <div class=\"progress\"> \n           <div class=\"progress-bar progress-bar-success progress-bar-striped active\" role=\"progressbar\" aria-valuenow=\"45\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width: 100%\"> \n           </div>\n          </div>\n         </div>\n        </div>\n       </div> \n       <p></p> \n       <p class=\"lead\" style=\"margin-top: 40px; margin-bottom: 30px;\">DNSdumpster.com is a FREE domain research tool that can discover hosts related to a domain. Finding visible hosts from the attackers perspective is an important part of the security assessment process.</p> \n      </div> \n      <p style=\"color: #777;\">this is a <a href=\"https://hackertarget.com/\" title=\"Online Vulnerability Scanners\"><button type=\"button\" class=\"btn btn-danger btn-xs\">HackerTarget.com</button></a> project</p> \n      <p><a href=\"#section2\"><i style=\"background-color: #333; color: #ccc; font-size: 3em; line-height: 5em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p> \n     </div> \n    </section> \n    <!-- Section 2 --> \n    <section id=\"home\" data-speed=\"4\" data-type=\"background\"> \n     <a name=\"section2\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-trash\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.7em; line-height: 1.9em; margin-bottom: 80px;\">Map an organizations attack surface with a virtual <i>dumpster dive*</i> of the DNS records associated with the target organization.</p> \n       <p style=\"font-size: 1.2em; color: #ccc;\">*DUMPSTER DIVING: The practice of sifting refuse from an office or technical installation to extract confidential data, especially security-compromising information.<br><span style=\"font-size: 0.9;\"><i>Dictionary.com</i></span></p> \n       <p><a href=\"#section3\"><i style=\"background-color: #333; color: #ccc; font-size: 2em; line-height: 4em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p>  \n      </div> \n     </div>\n    </section> \n    <!-- Section 3 --> \n    <section id=\"about\" data-speed=\"2\" data-type=\"background\"> \n     <a name=\"section3\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-cog\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.6em; line-height: 1.7em; margin-bottom: 80px;\">More than a simple <a href=\"https://hackertarget.com/dns-lookup/\" title=\"Online DNS Lookup\">DNS lookup</a> this tool will discover those hard to find sub-domains and web hosts. No brute force of common sub-domains is undertaken as is common practice for many DNS recon tools. The search relies on data from search engines and the excellent <a href=\"https://scans.io\" style=\"text-decoration: underline;\">scans.io</a> research project.</p> \n       <p><a href=\"#section4\"><i style=\"background-color: #333; color: #ccc; font-size: 2em; line-height: 4em;\" class=\"glyphicon glyphicon-chevron-down\"></i></a></p>  \n      </div> \n     </div>\n    </section> \n    <!-- Section 4 --> \n    <section id=\"about\" data-speed=\"2\" data-type=\"background\"> \n     <a name=\"section4\"></a> \n     <div class=\"container\">\n      <div class=\"col-md-2\"></div>\n      <div class=\"col-md-8\"> \n       <span class=\"glyphicon glyphicon-envelope\" style=\"font-size: 4em; line-height: 5.5em;\"></span> \n       <p style=\"font-size: 1.6em; line-height: 1.7em; margin-bottom: 80px;\">Receive updates by following <a href=\"https://twitter.com/hackertarget\" title=\"Twitter\" style=\"text-decoration: underline;\">@hackertarget</a> on Twitter<br> or subscribe to the low volume <a style=\"text-decoration: underline;\" href=\"http://eepurl.com/jDaVL\">mailing list</a>.</p>  \n       <p style=\"color: #777;\">this is a <a href=\"https://hackertarget.com/\" title=\"Online Vulnerability Scanners\"><button type=\"button\" class=\"btn btn-danger btn-xs\">HackerTarget.com</button></a> project</p>\n      </div> \n     </div>\n    </section> \n   </div> \n  </div> \n  <!-- Bootstrap core JavaScript\n    ================================================== --> \n  <!-- Placed at the end of the document so the pages load faster --> \n  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script> \n  <script src=\"/static/js/bootstrap.min.js\"></script> \n  <script src=\"/static/js/docs.min.js\"></script> \n  <!-- IE10 viewport hack for Surface/desktop Windows 8 bug --> \n  <script src=\"/static/js/ie10-viewport-bug-workaround.js\"></script> \n  <script>\n$(document).ready(function(){\n  $(\"#showloading\").hide();\n  $(\"#formsubmit\").click(function(){\n    $(\"#hideform\").hide();\n    $(\"#showloading\").show();\n  });\n\n});\n</script> \n  <script>\n  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n\n  ga('create', 'UA-2487671-6', 'auto');\n  ga('send', 'pageview');\n\n</script>   \n </body>\n</html>"
  val exampleToken = "BkgQgkNSuTrAWqLOYjk1z37GDh8vJbwK"

  val mockLogger: Logger = mock[Logger]
  val mockBrowser: Browser = mock[Browser]
  val mockExecutionContext: ExecutionContext = mock[ExecutionContext]
  val mockHttpExecutor: HttpExecutor = mock[HttpExecutor]

  behavior of "performScan"

  it should "perform a scan" in {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val stream : InputStream = getClass.getResourceAsStream("/DNSDumpster/ExampledomainExample.html")
    val html = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
    val hostname = "exampledomain.com"
    val subdomains =
      Set(
        "vps31252317.exampledomain.com",
        "www.exampledomain.com",
        "vps29782186.exampledomain.com",
        "vps57573743.exampledomain.com",
        "vps5025.exampledomain.com",
        "vps32272377.exampledomain.com",
        "vps22351531.exampledomain.com"
      )

    (mockLogger.logDNSDumpsterScanStarted _).expects()
    (mockLogger.logDNSDumpsterFoundSubdomains _).expects(subdomains)
    (mockLogger.logDNSDumpsterScanCompleted _).expects()

    (mockBrowser.get _).expects("https://dnsdumpster.com/").returns(JsoupBrowser().parseString(exampleHTMLWithToken))
    (mockBrowser.parseString _).expects(html).returns(JsoupBrowser().parseString(html))

    val mockResponse: Response = mock[Response]
    (mockResponse.getResponseBody _).expects().returns(html)

    (mockHttpExecutor.apply (_: Req)(_: ExecutionContext))
      .expects(*, ec)
      .returns(Future[Response](mockResponse))

    val scanner = new DNSDumpsterScanner(mockLogger, mockBrowser, mockHttpExecutor)

    val expectedSubdomains = subdomains
    val actualSubdomains = Await.result(scanner.performScan(hostname), TimeUtils.awaitDuration)

    assert(expectedSubdomains == actualSubdomains)
  }

  behavior of "retrieveHTMLWithTokenForHostname"

  it should "return the HTTP page" in {
    val token = exampleToken
    val hostname = "google.com"
    val html = "blaaaah"

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val mockResponse: Response = mock[Response]
    (mockResponse.getResponseBody _).expects().returns(html)

    (mockHttpExecutor.apply (_: Req)(_: ExecutionContext))
      .expects(*, ec)
      .returns(Future[Response](mockResponse))

    val scanner = new DNSDumpsterScanner(logger = mockLogger, http = mockHttpExecutor)

    val expectedHTML = html
    val actualHTML = Await.result(scanner.retrieveHTMLWithTokenForHostname(token, hostname), TimeUtils.awaitDuration)

    assert(expectedHTML == actualHTML)
  }

  behavior of "retrieveCSRFToken"

  it should "correctly find the CSRF token" in {
    (mockBrowser.get _).expects("https://dnsdumpster.com/").returns(JsoupBrowser().parseString(exampleHTMLWithToken))

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new DNSDumpsterScanner(mockLogger, mockBrowser)

    val expectedToken = exampleToken
    val actualToken = Try(Await.result(scanner.retrieveCSRFToken(), TimeUtils.awaitDuration))

    assert(actualToken.isSuccess)
    assert(expectedToken == actualToken.get)
  }

  it should "not find a CSRF token, throwing a TokenNotFoundException" in {
    (mockBrowser.get _).expects("https://dnsdumpster.com/").returns(JsoupBrowser().parseString(exampleHTMLWithoutToken))

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new DNSDumpsterScanner(mockLogger, mockBrowser)

    val actualToken = Try(Await.result(scanner.retrieveCSRFToken(), TimeUtils.awaitDuration))

    assert(actualToken.isFailure)
    assert(actualToken.failed.get.getClass == classOf[TokenNotFoundException])
    assert(actualToken.failed.get.getMessage == "No CSRF token was found.")
  }

  it should "not find a CSRF token, throwing an IOException" in {
    (mockBrowser.get _).expects("https://dnsdumpster.com/").throws(new IOException("Error!"))

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new DNSDumpsterScanner(mockLogger, mockBrowser)

    val actualToken = Try(Await.result(scanner.retrieveCSRFToken(), TimeUtils.awaitDuration))

    assert(actualToken.isFailure)
    assert(actualToken.failed.get.getClass == classOf[IOException])
    assert(actualToken.failed.get.getMessage == "Error!")
  }

  behavior of "extractSubdomains"

  it should "return no subdomains for html devoid of subdomains" in {
    val stream : InputStream = getClass.getResourceAsStream("/DNSDumpster/DNSDumpsterExample.html")
    val html = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
    val hostname = "dnsdumpster.com"

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = DNSDumpsterScanner.create(mockLogger)

    val expectedSubdomains = Set.empty
    val actualSubdomains = scanner.extractSubdomains(html, hostname)

    assert(expectedSubdomains == actualSubdomains)
  }

  it should "return no subdomains for html with subdomains for a different hostname" in {
    val stream : InputStream = getClass.getResourceAsStream("/DNSDumpster/ExampledomainExample.html")
    val html = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
    val hostname = "microsoft.com"

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = DNSDumpsterScanner.create(mockLogger)

    val expectedSubdomains = Set.empty
    val actualSubdomains = scanner.extractSubdomains(html, hostname)

    assert(expectedSubdomains == actualSubdomains)
  }

  it should "return the correct subdomains without the hostnmae" in {
    val stream : InputStream = getClass.getResourceAsStream("/DNSDumpster/ExampledomainExample.html")
    val html = scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
    val hostname = "exampledomain.com"

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = DNSDumpsterScanner.create(mockLogger)

    val expectedSubdomains =
      Set(
        "vps31252317.exampledomain.com",
        "www.exampledomain.com",
        "vps29782186.exampledomain.com",
        "vps57573743.exampledomain.com",
        "vps5025.exampledomain.com",
        "vps32272377.exampledomain.com",
        "vps22351531.exampledomain.com"
      )
    val actualSubdomains = scanner.extractSubdomains(html, hostname)

    assert(expectedSubdomains == actualSubdomains)
    assert(!actualSubdomains.contains(hostname))
  }

  behavior of "handleException"

  it should "log the exception message, returning an empty set" in {
    val msg = "Error!"
    val exception = new Exception(msg)

    (mockLogger.logDNSDumpsterConnectionError _).expects(msg)

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val scanner = new DNSDumpsterScanner(mockLogger, mockBrowser)

    val expectedSet = Set.empty
    val actualSet = scanner.handleException(exception)

    assert(expectedSet == actualSet)
  }

  behavior of "create"

  it should "return a DNSDumpsterScanner" in {
    val actualClass = DNSDumpsterScanner.create(mockLogger)(mockExecutionContext).getClass
    val expectedClass = classOf[DNSDumpsterScanner]

    assert(expectedClass == actualClass)
  }

  behavior of "TokenNotFoundException"

  it should "have the correct message" in {
    val msg = "Error!"

    val exception = new TokenNotFoundException(msg)

    val expectedMessage = msg
    val actualMessage = exception.getMessage

    assert(expectedMessage == actualMessage)
  }
}
