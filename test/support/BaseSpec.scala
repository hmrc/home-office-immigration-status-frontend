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

import org.mockito.Mockito.mock
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.{FakeRequest, Injecting}
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.play.PlayMongoModule
import uk.gov.hmrc.play.http.HeaderCarrierConverter

trait BaseSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach with Matchers with OptionValues {
 // given defaultTimeout: FiniteDuration = 5.seconds

//  object FakeAuditService extends AuditService {
//    def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(implicit
//      hc: HeaderCarrier,
//      request: Request[Any],
//      ec: ExecutionContext
//    ): Unit = ()
//  }
  protected val mockSessionCacheRepository: SessionCacheRepository    = mock(classOf[SessionCacheRepository])

  protected val modules: Seq[GuiceableModule] = Seq(
    bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
  )
  
  protected def appBuilder: GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .overrides(modules*)
      .disable[PlayMongoModule]
  }

  override implicit lazy val app: Application = appBuilder.build()

  //protected given materializer: Materializer = app.materializer

//  protected def checkHtmlResultWithBodyText(result: Future[Result], expectedSubstring: String): Unit = {
//    status(result)        mustBe 200
//    contentType(result)   mustBe Some("text/html")
//    charset(result)       mustBe Some("utf-8")
//    contentAsString(result) must include(expectedSubstring)
//    ()
//  }

//  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])

//  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(messages(key)).toString

  implicit def hc(implicit request: FakeRequest[?]): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)
}
