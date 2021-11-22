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

package views.components

import views.ViewSpec
import views.html.components.AlternateSearchLink

class AlternateSearchLinkSpec extends ViewSpec {

  val sut = inject[AlternateSearchLink]
  val doc = asDocument(sut("some.message.key", "/some/url")(messages))

  "alternate link" must {
    "have the link" in {
      val link = doc.getElementsByTag("a").first()

      link.text() mustBe "some.message.key"
      link.attr("href") mustBe "/some/url"
      assert(link.hasClass("govuk-link"))
    }
    "have the section break" in {
      val break = doc.getElementsByTag("hr").first()
      break.className() must include("govuk-section-break govuk-section-break--xl")
      break.className() must include("govuk-section-break--visible")
    }
  }

}
