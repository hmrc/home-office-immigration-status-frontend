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

import models.StatusCheckByNinoFormModel

import java.time.LocalDate
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, verify}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator
import views.html.StatusCheckFailurePage
import views.html.components.{SearchAgainButton, ShowChangeQuery}

class StatusCheckFailureViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])
  val mockSearchAgainButton: SearchAgainButton = mock(classOf[SearchAgainButton])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SearchAgainButton].toInstance(mockSearchAgainButton),
    )
    .build()

  lazy val sut: StatusCheckFailurePage = inject[StatusCheckFailurePage]

  val nino = NinoGenerator.generateNino
  val query = StatusCheckByNinoFormModel(nino, "Pan", "", LocalDate.now())
  lazy val doc: Document = asDocument(sut(query)(request, messages))

  "StatusCheckFailurePage" must {
    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-check-failure-title")

      e.text() mustBe messages("status-check-failure.title")
    }

    "have paragraph text" in {
      doc.select(".govuk-body").text() mustBe messages("status-check-failure.listParagraph")
    }

    "have the bullet points" when {
      List(1, 2).foreach { n =>
        s"is bullet point number $n" in {
          doc.getElementById(s"item$n").text() mustBe messages(s"status-check-failure.list-item$n")
        }
      }
    }

    "have personal details heading" in {
      val e: Element = doc.getElementById("personal-details")

      e.text() mustBe messages("status-check-failure.heading2CustomerDetails")
    }

    "have the show and change query section" in {
      verify(mockShowChangeQuery).apply(query)(messages)
    }

    "have the search again button" in {
      verify(mockSearchAgainButton).apply()(messages)
    }
  }
}
