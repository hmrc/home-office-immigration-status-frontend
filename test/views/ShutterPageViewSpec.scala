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

package views

import config.AppConfig
import org.mockito.Mockito.{mock, when}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import views.html.ShutteringPage

class ShutterPageViewSpec extends ViewSpec {

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AppConfig].toInstance(mockAppConfig)
    )
    .build()

  val providedShutterMessage = "[the provided message]"

  lazy val sut: ShutteringPage = inject[ShutteringPage]

  "apply" when {
    "a shutter message is provided" must {
      when(mockAppConfig.shutterMessage).thenReturn(Some(providedShutterMessage))
      val doc = asDocument(sut()(request, messages))

      "have the title" in {
        doc.getElementsByTag("h1").text() mustBe messages("shuttering.title")
      }

      "use the provided message" in {
        doc.html() must include(providedShutterMessage)
        doc.html() must not include messages("shuttering.defaultMessage")
      }
    }
    "no shutter message is provided" must {
      when(mockAppConfig.shutterMessage).thenReturn(None)
      val doc = asDocument(sut()(request, messages))

      "use the default message" in {
        doc.html() must include(messages("shuttering.defaultMessage"))
        doc.html() must not include providedShutterMessage
      }
    }
  }

}
