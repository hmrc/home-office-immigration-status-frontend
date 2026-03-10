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

package controllers

import config.AppConfig
import controllers.actions.AccessAction
import org.apache.pekko.util.Timeout
import org.mockito.Mockito.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SessionCacheRepository
import services.SessionCacheService
import support.BaseSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.duration.*
import scala.concurrent.{Await, Awaitable}
import scala.language.postfixOps

trait ControllerSpec extends BaseSpec {
  private val timeoutDuration: FiniteDuration                       = 5 seconds
  protected implicit val timeout: Timeout                             = Timeout(timeoutDuration)
  protected def await[T](future: Awaitable[T]): T                     = Await.result(future, timeoutDuration)
  protected lazy val messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  protected lazy val messages: Messages                               = messagesApi.preferred(Seq.empty)
  protected lazy val appConfig: AppConfig                             = app.injector.instanceOf[AppConfig]
  protected implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  protected val fakePostRequest: FakeRequest[AnyContentAsEmpty.type]  = FakeRequest("POST", "/")
  protected val mockSessionCacheService: SessionCacheService          = mock(classOf[SessionCacheService])
  protected implicit val hc: HeaderCarrier                            = HeaderCarrierConverter.fromRequest(request)

  override protected val modules: Seq[GuiceableModule] = Seq(
    bind[SessionCacheRepository].toInstance(mockSessionCacheRepository),
    bind[AccessAction].to[FakeAccessAction],
    bind[SessionCacheService].toInstance(mockSessionCacheService)
  )  
}
