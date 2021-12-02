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

import models.NinoSearchFormModel
import java.time.LocalDate

import config.AppConfig
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, when}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.SessionCacheService
import utils.NinoGenerator
import views.html.MultipleMatchesFoundPage
import views.html.components.{SearchAgainButton, ShowChangeQuery}

class MultipleMatchesFoundViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])
  val mockSearchAgainButton: SearchAgainButton = mock(classOf[SearchAgainButton])
  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SearchAgainButton].toInstance(mockSearchAgainButton),
      bind[AppConfig].toInstance(mockAppConfig)
    )
    .build()

  lazy val sut = inject[MultipleMatchesFoundPage]

  val nino = NinoGenerator.generateNino
  val query = NinoSearchFormModel(nino, "Pan", "", LocalDate.now())
  lazy val doc: Document = asDocument(sut(query, true)(request, messages))

  "MultipleMatchesFoundPage" must {

    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-check-failure-conflict-title")
      e.text() mustBe messages("status-check-failure-conflict.title")
    }

    "have personal details heading" in {
      val e: Element = doc.getElementById("personal-details")
      e.text() mustBe messages("status-check-failure.heading2CustomerDetails")
    }

    "have mrzlink" in {
      when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(true)
      lazy val mrzLinkDoc: Document = asDocument(sut(query, true)(request, messages))

      val e: Element = mrzLinkDoc.getElementById("mrzlink")
      e.text() mustBe messages("status-check-failure-conflict") + "Search by " + messages(
        "status-check-failure-conflict.passport")
    }

    "have ninolink" in {
      when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(true)
      lazy val ninoLinkDoc: Document = asDocument(sut(query, false)(request, messages))

      val e: Element = ninoLinkDoc.getElementById("ninolink")
      e.text() mustBe messages("status-check-failure-conflict") + "Search by " + messages(
        "status-check-failure-conflict.nino")
    }

    "mrzlink and ninolink do not show when feature disabled" in {
      //when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(false)
      assertNotRenderedById(doc, "nino-link")
      assertNotRenderedById(doc, "mrzlink")
    }

    "have the show and change query section" in {
      assertRenderedById(doc, "show-query")
    }

    "have the search again button" in {
      assertRenderedById(doc, "search-button")
    }
  }
}
