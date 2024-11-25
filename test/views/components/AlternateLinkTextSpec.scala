/*
 * Copyright 2024 HM Revenue & Customs
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

import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.ViewSpec
import views.html.components.AlternateLinkText

class AlternateLinkTextSpec extends ViewSpec {

  private val sut: AlternateLinkText = inject[AlternateLinkText]

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(isNinoSearch = true)(messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(isNinoSearch = true, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(true)(messages)

  "AlternateLinkText" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)

        "have the link" in {
          assertElementHasText(doc, "#alt-search-by-mrz", "search by passport or ID card")
          doc
            .getElementById("alt-search-by-mrz")
            .attr("href") mustBe "/check-immigration-status/search-by-passport?clearForm=true"
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => test.tupled(args))
  }
}
