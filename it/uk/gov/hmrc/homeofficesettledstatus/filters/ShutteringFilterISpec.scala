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

package uk.gov.hmrc.homeofficesettledstatus.filters

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import uk.gov.hmrc.homeofficesettledstatus.views.html.{error_template => ShutteringPage}
import uk.gov.hmrc.play.test.UnitSpec

class ShutteringFilterISpec extends ShutteringFilterISpecSetup {

  "The shuttering filter" should {
    "display the shuttering page when the `isShuttered` property is true" in {

      val ws = app.injector.instanceOf[WSClient]

      val result = await(ws.url(s"http://localhost:$port/check-settled-status").get)

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

trait ShutteringFilterISpecSetup extends UnitSpec with GuiceOneServerPerSuite {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("isShuttered" -> true)
    .build()

  implicit val configuration: Configuration = app.configuration
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])
}
