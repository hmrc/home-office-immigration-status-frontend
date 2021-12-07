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

import models.{MrzSearchFormModel, NinoSearchFormModel}
import java.time.LocalDate

import config.AppConfig
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, verify, when}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.SessionCacheService
import utils.NinoGenerator
import views.html.StatusCheckFailurePage
import views.html.components.{SearchAgainButton, ShowChangeQuery}

class StatusCheckFailureViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])
  val mockSearchAgainButton: SearchAgainButton = mock(classOf[SearchAgainButton])
  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  val mockSessionCacheService: SessionCacheService = mock(classOf[SessionCacheService])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SearchAgainButton].toInstance(mockSearchAgainButton),
      bind[AppConfig].toInstance(mockAppConfig),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  lazy val sut: StatusCheckFailurePage = inject[StatusCheckFailurePage]

  val nino = NinoGenerator.generateNino
  val ninSearchFormModel = NinoSearchFormModel(nino, "Pan", "", LocalDate.now())
  val mrzSearchFormModel = MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")

  when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(false)
  val DocWithoutFeature: Document = asDocument(sut(ninSearchFormModel)(request, messages))

  when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(true)
  val NinoDocWithFeature: Document = asDocument(sut(ninSearchFormModel)(request, messages))
  val MrzDocWithFeature: Document = asDocument(sut(mrzSearchFormModel)(request, messages))

  "StatusCheckFailurePage" must {
    "have a status conflict title" in {
      val e: Element = DocWithoutFeature.getElementById("status-check-failure-title")
      e.text() mustBe messages("status-check-failure.title")
    }

    "have paragraph text" in {
      DocWithoutFeature.select(".govuk-body").text() mustBe messages("status-check-failure.listParagraph")
    }

    "have the bullet points" when {
      List(1, 2).foreach { n =>
        s"is bullet point number $n" in {
          DocWithoutFeature.getElementById(s"item$n").text() mustBe messages(s"status-check-failure.list-item$n")
        }
      }
    }

    "have personal details heading" in {
      val e: Element = DocWithoutFeature.getElementById("personal-details")
      e.text() mustBe messages("status-check-failure.heading2CustomerDetails")
    }

    "nino doc has mrzlink" in {
      val e: Element = NinoDocWithFeature.getElementById("mrzlink")
      e.text() mustBe messages("status-check-failure-conflict") + messages("status-check-failure-conflict.mrz-link")
    }

    "mrz doc has ninolink" in {
      val e: Element = MrzDocWithFeature.getElementById("ninolink")
      e.text() mustBe messages("status-check-failure-conflict") + messages("status-check-failure-conflict.nino-link")
    }

    "mrzlink and ninolink do not show when feature disabled" in {
      assertNotRenderedById(DocWithoutFeature, "ninolink")
      assertNotRenderedById(DocWithoutFeature, "mrzlink")
    }

    "have the show and change query section" in {
      assertRenderedById(DocWithoutFeature, "inputted-data")
    }

    "have the search again button" in {
      assertRenderedByCssSelector(DocWithoutFeature, ".govuk-button")
    }
  }
}
