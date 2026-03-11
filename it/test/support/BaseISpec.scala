/*
 * Copyright 2026 HM Revenue & Customs
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

import config.AppConfig
import models.{FormQueryModel, Search, StatusCheckResponseWithStatus}
import org.apache.pekko.stream.Materializer
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.{charset, contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Environment}
import play.twirl.api.HtmlFormat
import repositories.*
import services.AuditService
import stubs.AuthStubs
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.PlayMongoModule
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
trait BaseISpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneServerPerSuite
    with OptionValues
    with WireMockSupport
    with AuthStubs
    with Injecting
    with ScalaFutures
    with IntegrationPatience
    with DefaultPlayMongoRepositorySupport[FormQueryModel] {

  override protected def checkIndexedQueries = false

  override lazy val app: Application = appBuilder.build()

  override val repository: SessionCacheRepositoryImpl = app.injector.instanceOf[SessionCacheRepositoryImpl]

  given defaultTimeout: FiniteDuration = 5.seconds

  object FakeAuditService extends AuditService {
    def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(implicit
      hc: HeaderCarrier,
      request: Request[Any],
      ec: ExecutionContext
    ): Unit = ()
  }

  protected def appBuilder: GuiceApplicationBuilder =
    GuiceApplicationBuilder(
      environment = Environment.simple(),
      overrides = Seq(
        bind[AuditService].toInstance(FakeAuditService),
        bind[MongoComponent].toInstance(mongoComponent)
      )
    ).configure(
      "auditing.enabled"                                                -> false,
      "metrics.enabled"                                                 -> false,
      "isShuttered"                                                     -> false,
      "play.filters.csrf.header.bypassHeaders.Csrf-Token"               -> "nocheck",
      "microservice.services.auth.host"                                 -> wireMockHost,
      "microservice.services.auth.port"                                 -> wireMockPort,
      "microservice.services.home-office-immigration-status-proxy.host" -> wireMockHost,
      "microservice.services.home-office-immigration-status-proxy.port" -> wireMockPort
    )

  protected given materializer: Materializer = app.materializer

  protected def checkHtmlResultWithBodyText(result: Future[Result], expectedSubstring: String): Unit = {
    status(result) mustBe 200
    contentType(result) mustBe Some("text/html")
    charset(result) mustBe Some("utf-8")
    contentAsString(result) must include(expectedSubstring)
    ()
  }

  implicit lazy val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(messages(key)).toString

  implicit def hc(implicit request: FakeRequest[?]): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)
}
