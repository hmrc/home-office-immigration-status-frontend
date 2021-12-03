package support

import akka.stream.Materializer
import models.{Search, StatusCheckError, StatusCheckResponseWithStatus, StatusCheckSuccessfulResponse}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.{charset, contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import services.AuditService
import stubs.{AuthStubs, DataStreamStubs}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait BaseISpec
    extends AnyWordSpecLike
      with Matchers
      with GuiceOneServerPerSuite
      with OptionValues
      with ScalaFutures
      with WireMockSupport
      with AuthStubs
      with DataStreamStubs
      with MetricsTestSupport
      with Injecting {

  override def fakeApplication: Application = appBuilder.build()

  implicit val defaultTimeout: FiniteDuration = 5 seconds

  object FakeAuditService extends AuditService {
    def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(
      implicit hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext): Unit = ()
  }

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "isShuttered" -> false,
        "metrics.enabled"                -> true,
        "auditing.enabled"                     -> true,
        "auditing.consumer.baseUri.host"       -> wireMockHost,
        "auditing.consumer.baseUri.port"       -> wireMockPort,
        "play.filters.csrf.method.whiteList.0" -> "POST",
        "play.filters.csrf.method.whiteList.1" -> "GET",
        "microservice.services.auth.host" -> wireMockHost,
        "microservice.services.auth.port" -> wireMockPort,
        "microservice.services.home-office-immigration-status-proxy.host" -> wireMockHost,
        "microservice.services.home-office-immigration-status-proxy.port" -> wireMockPort,
        "feature-switch.mrz.enabled" -> true
      ).overrides(
        bind[AuditService].toInstance(FakeAuditService)
      )

  override def commonStubs(): Unit = {
    givenCleanMetricRegistry()
    givenAuditConnector()
  }

  protected implicit val materializer: Materializer = app.materializer

  protected def checkHtmlResultWithBodyText(result: Future[Result], expectedSubstring: String): Unit = {
    status(result) shouldBe 200
    contentType(result) shouldBe Some("text/html")
    charset(result) shouldBe Some("utf-8")
    contentAsString(result) should include(expectedSubstring)
  }

  implicit lazy val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(messages(key)).toString

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)
}
