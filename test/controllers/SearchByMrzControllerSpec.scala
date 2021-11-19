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

import controllers.actions.AccessAction
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, status}
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import views.html.SearchByMrzView

import scala.concurrent.Future

class SearchByMrzControllerSpec extends ControllerSpec {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AccessAction].to[FakeAccessAction],
      bind[SearchByMrzView].toInstance(mockView),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  lazy val sut = inject[SearchByMrzController]
  val mockView = mock(classOf[SearchByMrzView])
  val fakeView = HtmlFormat.escape("Correct Form View")

  override def beforeEach(): Unit = {
    reset(mockView)
    when(mockView()(any(), any())).thenReturn(fakeView)
    reset(mockSessionCacheService)
    super.beforeEach()
  }

  "onPageLoad" must {
    "display the check by passport view" when {
      "request is made" in {
        val result = sut.onPageLoad(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
      }
    }
  }

}
