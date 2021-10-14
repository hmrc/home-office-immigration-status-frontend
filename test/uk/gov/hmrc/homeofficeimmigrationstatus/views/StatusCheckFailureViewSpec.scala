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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import org.joda.time.LocalDate
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api.inject.bind
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoRequest
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.StatusCheckFailurePage
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.components.{SearchAgainButton, ShowChangeQuery}

class StatusCheckFailureViewSpec extends ViewSpec {

  val mockShowChangeQuery: ShowChangeQuery = mock(classOf[ShowChangeQuery])
  val mockSearchAgainButton: SearchAgainButton = mock(classOf[SearchAgainButton])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ShowChangeQuery].toInstance(mockShowChangeQuery),
      bind[SearchAgainButton].toInstance(mockSearchAgainButton),
    )
    .build()

  when(mockShowChangeQuery.apply(any())(any())).thenReturn(HtmlFormat.empty)
  when(mockSearchAgainButton.apply()(any())).thenReturn(HtmlFormat.empty)

  lazy val sut: StatusCheckFailurePage = inject[StatusCheckFailurePage]

  //todo nino gen
  val query = StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)
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
