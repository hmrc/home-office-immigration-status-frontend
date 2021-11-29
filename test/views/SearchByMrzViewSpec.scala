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

import forms.SearchByMRZForm
import models.{MrzSearch, MrzSearchFormModel}
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api.inject.bind
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import views.html.SearchByMrzView
import views.html.components.{AlternateSearchLink, inputDate}
import java.util.UUID

class SearchByMrzViewSpec extends ViewSpec {

  val mockAlternateSearch = mock(classOf[AlternateSearchLink])
  val mockDobInput = mock(classOf[inputDate])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AlternateSearchLink].toInstance(mockAlternateSearch),
      bind[inputDate].toInstance(mockDobInput),
    )
    .build()

  val fakeAlternativeSearch: String = UUID.randomUUID().toString
  when(mockAlternateSearch.apply(any(), any(), any())(any())).thenReturn(Html(fakeAlternativeSearch))
  val fakeDobInput: String = UUID.randomUUID().toString
  when(mockDobInput.apply(any(), any(), any(), any(), any(), any(), any())(any())).thenReturn(Html(fakeDobInput))

  lazy val sut: SearchByMrzView = inject[SearchByMrzView]

  val form: Form[MrzSearchFormModel] = inject[SearchByMRZForm].apply()

  lazy val doc: Document = asDocument(sut(form)(request, messages))

  "SearchByMrzView" must {
    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.title")
    }

    "have the alternate search link" in {
      doc.text() must include(fakeAlternativeSearch)
      verify(mockAlternateSearch)
        .apply(
          "alternate-search.nino-link",
          controllers.routes.StatusCheckByNinoController.onPageLoad.url,
          "alt-search-by-nino")(messages)
    }

    "have identity doc" in {
      assertRenderedById(doc, "documenttype")
    }

    "have the identity component contains options" in {
      val e: Element = doc.getElementById("documenttype")
      e.text() mustBe messages(
        "Passport European National Insurance Card Biometric Residence Card Biometric Residence Permit")
    }

    "have the nationality select" in {
      assertRenderedById(doc, "nationality")
    }

    "have the dob input" in {
      doc.text() must include(fakeDobInput)
      verify(mockDobInput)
        .apply(
          form,
          id = "dateOfBirth",
          legendClasses = "govuk-label",
          legendContent = messages("lookup.dateOfBirth.label"),
          hintMessage = Some(messages("lookup.dateOfBirth.hint"))
        )(messages)
    }

    "have the search button" in {
      assertRenderedById(doc, "search-button")
    }
  }
}
