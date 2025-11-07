/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support

import models.{Search, StatusCheckResponseWithStatus}
import org.apache.pekko.stream.Materializer
import org.scalatest.OptionValues
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
import stubs.AuthStubs
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait BaseISpec
    extends AnyWordSpecLike
    with Matchers
    with GuiceOneServerPerSuite
    with OptionValues
    with WireMockSupport
    with AuthStubs
    with Injecting {

  override def fakeApplication(): Application = appBuilder.build()

  given defaultTimeout: FiniteDuration = 5.seconds

  object FakeAuditService extends AuditService {
    def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(implicit
      hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext
    ): Unit = ()
  }

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "auditing.enabled"                                                -> false,
        "metrics.enabled"                                                 -> false,
        "isShuttered"                                                     -> false,
        "play.filters.csrf.header.bypassHeaders.Csrf-Token"               -> "nocheck",
        "microservice.services.auth.host"                                 -> wireMockHost,
        "microservice.services.auth.port"                                 -> wireMockPort,
        "microservice.services.home-office-immigration-status-proxy.host" -> wireMockHost,
        "microservice.services.home-office-immigration-status-proxy.port" -> wireMockPort
      )
      .overrides(
        bind[AuditService].toInstance(FakeAuditService)
      )

  protected given materializer: Materializer = app.materializer

  protected def checkHtmlResultWithBodyText(result: Future[Result], expectedSubstring: String): Unit = {
    status(result)        shouldBe 200
    contentType(result)   shouldBe Some("text/html")
    charset(result)       shouldBe Some("utf-8")
    contentAsString(result) should include(expectedSubstring)
    ()
  }

  implicit lazy val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(messages(key)).toString

  implicit def hc(implicit request: FakeRequest[?]): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)
}
