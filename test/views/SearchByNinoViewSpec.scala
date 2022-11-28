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

import config.AppConfig
import forms.SearchByNinoForm
import models.NinoSearchFormModel
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, when}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import repositories.SessionCacheRepository
import views.html.SearchByNinoView
import views.html.components.inputDate

import java.util.UUID

class SearchByNinoViewSpec extends ViewSpec {

  val mockDobInput: inputDate  = mock(classOf[inputDate])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[inputDate].toInstance(mockDobInput),
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository),
      bind[AppConfig].toInstance(mockAppConfig)
    )
    .build()

  val fakeDobInput: String = UUID.randomUUID().toString

  lazy val sut: SearchByNinoView = inject[SearchByNinoView]

  val form: Form[NinoSearchFormModel] = inject[SearchByNinoForm].apply()

  def createDocument: Document = {
    reset(mockDobInput)
    when(mockDobInput.apply(any(), any(), any(), any(), any(), any(), any())(any()))
      .thenReturn(Html(fakeDobInput))
    reset(mockAppConfig)

    asDocument(sut(form)(request, messages))
  }

  "With the document search enabled, the view" must {
    lazy val doc = createDocument

    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.nino.title")
    }

    "have the search description" in {
      val e: Element = doc.getElementById("search-description")
      e.text() mustBe s"${messages("lookup.nino.desc")}${messages("lookup.nino.alternate-search")}."
    }

    "have the alternative search link" in {
      val e: Element = doc.getElementById("alt-search-by-mrz")
      e.text() mustBe messages("lookup.nino.alternate-search")
      e.attr("href") mustBe controllers.routes.SearchByMrzController.onPageLoad(true).url
    }

    "have nino" in {
      assertRenderedById(doc, "nino")
    }

    "have givenName" in {
      assertRenderedById(doc, "givenName")
    }

    "have familyName" in {
      assertRenderedById(doc, "familyName")
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
