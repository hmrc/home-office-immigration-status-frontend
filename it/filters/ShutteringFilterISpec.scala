/*
 * Copyright 2020 HM Revenue & Customs
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

package filters

import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.i18n.Messages
import play.api.test.Helpers.await
import support.NonAuthPageISpec
import views.html.{error_template => ShutteringPage}

class ShutteringFilterISpec extends NonAuthPageISpec("isShuttered" -> true) {

  "The shuttering filter" should {
    "display the shuttering page when the `isShuttered` property is true" in {

      val result = await(ws.url(s"http://localhost:$port/check-immigration-status").get)

      result.status shouldBe SERVICE_UNAVAILABLE

      val shutteringPage = app.injector.instanceOf[ShutteringPage]

      result.body shouldBe shutteringPage(
        Messages("shuttering.title"),
        Messages("shuttering.heading"),
        Messages("global.error.500.message")
      ).toString
    }
  }
}
