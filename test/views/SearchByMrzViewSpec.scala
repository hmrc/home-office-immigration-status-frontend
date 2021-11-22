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

import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import play.api
import play.api.inject.bind
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import views.html.SearchByMrzView
import views.html.components.AlternateSearchLink

import java.util.UUID

class SearchByMrzViewSpec extends ViewSpec {

  val mockAlternateSearch = mock(classOf[AlternateSearchLink])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AlternateSearchLink].toInstance(mockAlternateSearch)
    )
    .build()

  val fakeAlternativeSearch: String = UUID.randomUUID().toString
  when(mockAlternateSearch.apply(any(), any())(any())).thenReturn(Html(fakeAlternativeSearch))

  lazy val sut: SearchByMrzView = inject[SearchByMrzView]

  lazy val doc: Document = asDocument(sut()(request, messages))

  "SearchByMrzView" must {
    "have the look up title" in {
      val e: Element = doc.getElementsByTag("h1").first()
      e.text() mustBe messages("lookup.title")
    }
    "have the alternate search link" in {
      doc.text() must include(fakeAlternativeSearch)
      verify(mockAlternateSearch)
        .apply("alternate-search.nino-link", controllers.routes.StatusCheckByNinoController.onPageLoad.url)(messages)
    }
  }
}