package uk.gov.hmrc.homeofficesettledstatus.support

import akka.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{charset, contentAsString, contentType, defaultAwaitTimeout, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.homeofficesettledstatus.stubs.{AuthStubs, DataStreamStubs}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.Future
import scala.language.postfixOps
import scala.reflect.ClassTag

abstract class BaseISpec
    extends AnyWordSpecLike with Matchers with OptionValues with WireMockSupport with AuthStubs with DataStreamStubs
    with MetricsTestSupport {

  import scala.concurrent.duration._
  implicit val defaultTimeout: FiniteDuration = 5 seconds

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                -> true,
        "auditing.enabled"                     -> true,
        "auditing.consumer.baseUri.host"       -> wireMockHost,
        "auditing.consumer.baseUri.port"       -> wireMockPort,
        "play.filters.csrf.method.whiteList.0" -> "POST",
        "play.filters.csrf.method.whiteList.1" -> "GET",
        "microservice.services.auth.host" -> wireMockHost,
        "microservice.services.auth.port" -> wireMockPort,
        "microservice.services.home-office-immigration-status-proxy.host" -> wireMockHost,
        "microservice.services.home-office-immigration-status-proxy.port" -> wireMockPort
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

  def injected[T](implicit evidence: ClassTag[T]): T = app.injector.instanceOf[T]
  lazy val messagesApi: MessagesApi = injected[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)
}
