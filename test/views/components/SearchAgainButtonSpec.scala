/*
 * Copyright 2023 HM Revenue & Customs
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

package views.components

import controllers.routes
import org.jsoup.nodes.Document
import views.ViewSpec
import views.html.components.SearchAgainButton

class SearchAgainButtonSpec extends ViewSpec {

  val sut: SearchAgainButton = inject[SearchAgainButton]
  val doc: Document          = asDocument(sut()(messages))

  "SearchAgainButton" must {
    val button = doc.select("a")
    "have the content" in {
      button.text() mustBe messages("generic.searchAgain")
    }
    "link to the correct place" in {
      button.attr("href") mustBe routes.LandingController.onPageLoad.url
    }

    "have id search-again-button" in {
      button.attr("id") mustBe "search-again-button"
    }
  }
}
