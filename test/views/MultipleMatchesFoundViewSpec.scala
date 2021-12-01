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
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, verify}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import utils.NinoGenerator
import views.html.MultipleMatchesFoundPage
import views.html.components.{SearchAgainButton, ShowChangeQuery}

class MultipleMatchesFoundViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])
  val mockSearchAgainButton: SearchAgainButton = mock(classOf[SearchAgainButton])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SearchAgainButton].toInstance(mockSearchAgainButton)
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
      val e: Element = doc.getElementById("mrzlink")
      e.text() mustBe messages("status-check-failure-conflict") + "Search by " + messages(
        "status-check-failure-conflict.passport")
    }

    "have ninolink" in {
      lazy val ninoLinkDoc: Document = asDocument(sut(query, false)(request, messages))
      val e: Element = ninoLinkDoc.getElementById("ninolink")
      e.text() mustBe messages("status-check-failure-conflict") + "Search by " + messages(
        "status-check-failure-conflict.nino")
    }

    "have the show and change query section" in {
      verify(mockShowChangeQuery).apply(query)(messages)
    }

    "have the search again button" in {
      verify(mockSearchAgainButton).apply()(messages)
    }
  }
}
