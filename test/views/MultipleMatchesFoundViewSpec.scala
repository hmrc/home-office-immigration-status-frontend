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
import org.joda.time.LocalDate
import org.jsoup.nodes.{Document, Element}
import org.mockito.Mockito.{mock, verify}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
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

  //todo nino gen
  val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)
  lazy val doc: Document = asDocument(sut(query)(request, messages))

  "MultipleMatchesFoundPage" must {

    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-check-failure-conflict-title")
      e.text() mustBe messages("status-check-failure-conflict.title")
    }

    "have paragraph text" in {
      doc.select(".govuk-body").text() mustBe messages("status-check-failure-conflict.listParagraph")
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