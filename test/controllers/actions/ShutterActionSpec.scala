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

package controllers.actions

import org.mockito.Mockito.{mock, reset, verify, when}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import config.AppConfig
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.mvc._
import play.api.test.FakeRequest
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import play.api.i18n.Messages
import play.api.i18n.{Lang, Messages, MessagesApi}
import views.html.ShutteringPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import services.SessionCacheService
import play.api.test.Helpers.contentAsString
import controllers.ControllerSpec

import scala.concurrent.Future

class ShutterActionSpec extends ControllerSpec {

  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AppConfig].toInstance(mockAppConfig),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  implicit val req = request
  implicit val mess = messages
  lazy val shutteringPage = inject[ShutteringPage]

  lazy val shutterAction = inject[ShutterAction]

  override def beforeEach(): Unit = {
    reset(mockAppConfig)
    super.beforeEach
  }
  val block: Request[_] => Future[Result] = request => Future.successful(Ok("Invoked"))

  "applyFiltering" must {

    "don't filter where shuttered is false" in {
      when(mockAppConfig.shuttered).thenReturn(false)
      val fut = shutterAction.invokeBlock(request, block)
      contentAsString(fut) mustEqual "Invoked"
    }

    "filter to the shutter page where shuttered is true" in {
      when(mockAppConfig.shuttered).thenReturn(true)
      val fut = shutterAction.invokeBlock(request, block)
      contentAsString(fut) mustEqual shutteringPage().toString
    }

  }
}
