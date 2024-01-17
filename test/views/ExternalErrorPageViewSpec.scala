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

package views

import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.html.ExternalErrorPage

class ExternalErrorPageViewSpec extends ViewSpec {

  private val sut: ExternalErrorPage = inject[ExternalErrorPage]

  private val titleAndHeading: String = "Sorry, there is a problem with the service"

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply()(request, messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(request, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f()(request, messages)

  "ExternalErrorPageView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the title and heading" in {
          assertElementHasText(doc, "title", s"$titleAndHeading - Check immigration status - GOV.UK")
          assertElementHasText(doc, "#external-error-title", titleAndHeading)
        }

        "have the first paragraph content" in {
          assertElementHasText(doc, ".govuk-body", "Try again later.")
        }

        "have the second paragraph content" in {
          doc
            .select(".govuk-body")
            .next()
            .text() mustBe "Contact the IT help desk (opens in new tab) to report this problem."
        }

        "have the link text" in {
          assertElementHasText(doc, ".govuk-body > a", "Contact the IT help desk (opens in new tab)")
        }

        "have the link" in {
          doc.select(".govuk-body > a").attr("href") must endWith("/it-helpdesk/get-help-it-problem")
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
