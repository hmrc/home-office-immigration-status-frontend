/*
 * Copyright 2022 HM Revenue & Customs
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

import models.{MrzSearchFormModel, NinoSearchFormModel}
import java.time.LocalDate

import config.AppConfig
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, when}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.SessionCacheRepository
import services.SessionCacheService
import utils.NinoGenerator
import views.html.MultipleMatchesFoundPage

class MultipleMatchesFoundViewSpec extends ViewSpec {

  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AppConfig].toInstance(mockAppConfig),
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  lazy val sut = inject[MultipleMatchesFoundPage]
  val nino = NinoGenerator.generateNino

  val ninSearchFormModel = NinoSearchFormModel(nino, "Pan", "", LocalDate.now())
  val mrzSearchFormModel = MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")

  val NinoDocWithFeature: Document = asDocument(sut(ninSearchFormModel)(request, messages))
  val MrzDocWithFeature: Document = asDocument(sut(mrzSearchFormModel)(request, messages))

  "MultipleMatchesFoundPage" must {
    "have a status conflict title" in {
      val e: Element = MrzDocWithFeature.getElementById("status-check-failure-conflict-title")
      e.text() mustBe messages("status-check-failure-conflict.title")
    }

    "have personal details heading" in {
      val e: Element = MrzDocWithFeature.getElementById("personal-details")
      e.text() mustBe messages("status-check-failure.heading2CustomerDetails")
    }

    "nino doc has mrz alt link" in {
      assertRenderedById(NinoDocWithFeature, "alternate-search")
      val e: Element = NinoDocWithFeature.getElementById("alt-search-by-mrz")
      e.text() mustBe messages("status-check-failure-conflict.mrz-link")
    }

    "mrz doc has nino alt link" in {
      assertRenderedById(MrzDocWithFeature, "alternate-search")
      val e: Element = MrzDocWithFeature.getElementById("alt-search-by-nino")
      e.text() mustBe messages("status-check-failure-conflict.nino-link")
    }

    "have the show and multiple label" in {
      assertRenderedById(MrzDocWithFeature, "multiplelabel")
    }

    "have the show and change query section" in {
      assertRenderedById(MrzDocWithFeature, "inputted-data")
    }

    "have the search again button" in {
      assertRenderedByCssSelector(MrzDocWithFeature, ".govuk-button")
    }
  }
}
