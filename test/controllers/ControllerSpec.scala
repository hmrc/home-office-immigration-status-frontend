/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.util.Timeout
import config.AppConfig
import controllers.actions.AuthAction
import org.mockito.Mockito.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import services.SessionCacheService

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}
import scala.language.postfixOps

trait ControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to[FakeAuthAction],
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  val timeoutDuration: FiniteDuration = 5 seconds
  implicit val timeout: Timeout = Timeout(timeoutDuration)
  def await[T](future: Awaitable[T]): T = Await.result(future, timeoutDuration)
  lazy val messages: Messages = inject[MessagesApi].preferred(Seq.empty)
  lazy val appConfig: AppConfig = inject[AppConfig]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val mockSessionCacheService: SessionCacheService = mock(classOf[SessionCacheService])
}