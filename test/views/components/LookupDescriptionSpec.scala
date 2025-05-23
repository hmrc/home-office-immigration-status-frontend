/*
 * Copyright 2025 HM Revenue & Customs
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
import views.html.components.LookupDescription

class LookupDescriptionSpec extends ViewSpec {

  private val message: String  = "some.message.key"
  private val linkText: String = "link text"
  private val url: String      = "/some/url"
  private val id: String       = "link-id"

  private val sut: LookupDescription = inject[LookupDescription]

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(message, linkText, url, id)(messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(message, linkText, url, id, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(message, linkText, url, id)(messages)

  "LookupDescription" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the search description paragraph content" in {
          assertElementHasText(doc, "#search-description", "some.message.key link text.")
        }

        "have the link" in {
          assertElementHasText(doc, "#link-id", "link text")
          doc.getElementById("link-id").attr("href") mustBe "/some/url"
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
