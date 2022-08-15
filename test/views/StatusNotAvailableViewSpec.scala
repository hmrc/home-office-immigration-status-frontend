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

import models.{NinoSearchFormModel, StatusCheckResult}
import java.time.LocalDate

import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.mock
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.SessionCacheRepository
import utils.NinoGenerator.generateNino
import views.html.StatusNotAvailablePage
import views.html.components.{SearchAgainButton, ShowChangeQuery}

class StatusNotAvailableViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  lazy val sut: StatusNotAvailablePage = inject[StatusNotAvailablePage]

  val nino   = NinoSearchFormModel(generateNino, "Applicant", "", LocalDate.now())
  val result = StatusCheckResult("Full name", LocalDate.now(), "JPN", Nil)

  val query = StatusNotAvailablePageContext(nino, result)

  lazy val doc: Document = asDocument(sut(query)(request, messages))

  "StatusNotAvailable" must {
    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-not-available-title")
      e.text() mustBe "Full name " + messages("app.hasNoActiveStatus")
    }

    "status has paragraph list" in {
      assertElementHasText(doc, "#not-available-paragraph", messages("status-not-available.listParagraph"))
    }

    "have a status list content" in {
      assertElementHasText(
        doc,
        "#not-available-list",
        messages("status-not-available.list-item1") + " "
          + messages("status-not-available.list-item2")
      )
    }

    "have the summary list" in {
      assertRenderedById(doc, "notAvailablePersonalData")
    }

    "have the search again button" in {
      val button = doc.select("#content > a")
      button.text() mustBe "Search again"
      button.attr("href") mustBe "/check-immigration-status"
    }
  }
}
