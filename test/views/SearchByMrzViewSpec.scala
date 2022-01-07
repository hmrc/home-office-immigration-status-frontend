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

import forms.SearchByMRZForm
import models.MrzSearchFormModel
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api.inject.bind
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import views.html.SearchByMrzView
import views.html.components.inputDate
import java.util.UUID

class SearchByMrzViewSpec extends ViewSpec {

  val mockDobInput = mock(classOf[inputDate])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[inputDate].toInstance(mockDobInput),
    )
    .build()

  val fakeDobInput: String = UUID.randomUUID().toString
  when(mockDobInput.apply(any(), any(), any(), any(), any(), any(), any())(any())).thenReturn(Html(fakeDobInput))

  lazy val sut: SearchByMrzView = inject[SearchByMrzView]

  val form: Form[MrzSearchFormModel] = inject[SearchByMRZForm].apply()

  lazy val doc: Document = asDocument(sut(form)(request, messages))

  "SearchByMrzView" must {
    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.mrz.title")
    }

    "have the search description" in {
      val e: Element = doc.getElementById("search-description")
      e.text() mustBe s"${messages("lookup.mrz.desc")}${messages("lookup.mrz.alternate-search")}."
    }

    "have the alternative search link" in {
      val e: Element = doc.getElementById("alternate-search")
      e.text() mustBe messages("lookup.mrz.alternate-search")
      e.attr("href") mustBe controllers.routes.SearchByNinoController.onPageLoad(true).url
    }

    "have documentType" in {
      assertRenderedById(doc, "documentType")
    }

    "have the identity component contains options" in {
      val e: Element = doc.getElementById("documentType")
      e.text() mustBe messages(
        "Passport European National Identity Card Biometric Residence Card Biometric Residence Permit")
    }

    "have documentNumber" in {
      assertRenderedById(doc, "documentNumber")
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
