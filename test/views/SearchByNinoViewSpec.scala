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

import forms.SearchByNinoForm
import models.{NinoSearch, NinoSearchFormModel}
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, when}
import play.api.inject.bind
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import views.html.SearchByNinoView
import views.html.components.{AlternateSearchLink, inputDate}
import java.util.UUID
import config.AppConfig
import services.SessionCacheService

class SearchByNinoViewSpec extends ViewSpec {

  val mockAlternateSearch = mock(classOf[AlternateSearchLink])
  val mockDobInput = mock(classOf[inputDate])
  val mockAppConfig = mock(classOf[AppConfig])
  val mockSessionCacheService = mock(classOf[SessionCacheService])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AlternateSearchLink].toInstance(mockAlternateSearch),
      bind[inputDate].toInstance(mockDobInput),
      bind[SessionCacheService].toInstance(mockSessionCacheService),
      bind[AppConfig].toInstance(mockAppConfig)
    )
    .build()

  val fakeAlternativeSearch: String = UUID.randomUUID().toString
  val fakeDobInput: String = UUID.randomUUID().toString

  lazy val sut: SearchByNinoView = inject[SearchByNinoView]

  val form: Form[NinoSearchFormModel] = inject[SearchByNinoForm].apply()

  def createDocument(documentSearchEnabled: Boolean): Document = {
    reset(mockAlternateSearch)
    when(mockAlternateSearch.apply(any(), any(), any())(any())).thenReturn(Html(fakeAlternativeSearch))
    reset(mockDobInput)
    when(mockDobInput.apply(any(), any(), any(), any(), any(), any(), any())(any()))
      .thenReturn(Html(fakeDobInput))
    reset(mockAppConfig)
    when(mockAppConfig.documentSearchFeatureEnabled).thenReturn(documentSearchEnabled)

    asDocument(sut(form)(request, messages))
  }

  "With the document search enabled, the view" must {
    lazy val doc = createDocument(true)

    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.title")
    }

    "have the alternate search link" in {
      doc.text() must include(fakeAlternativeSearch)
      verify(mockAlternateSearch)
        .apply(
          "alternate-search.mrz-link",
          controllers.routes.SearchByMrzController.onPageLoad.url,
          "alt-search-by-mrz")(messages)
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

  "With the document search disabled, the view" must {
    lazy val doc = createDocument(false)

    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.title")
    }

    "NOT have the alternate search link" in {
      doc.text() must not include (fakeAlternativeSearch)
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
