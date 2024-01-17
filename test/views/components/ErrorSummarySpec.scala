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
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import views.html.components.errorSummary
import views.ViewSpec

class ErrorSummarySpec extends ViewSpec {

  private val sut: errorSummary = inject[errorSummary]

  private val errors: Seq[FormError] = Seq(FormError("nino", List("error.nino.required")))

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(errors)(messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(errors, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(errors)(messages)

  "ErrorSummary" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the heading" in {
          assertElementHasText(doc, "h2", "There is a problem")
        }

        "have the error message" in {
          assertElementHasText(doc, "#nino-error-summary", "Enter a National Insurance number in the correct format")
        }

        "have the link" in {
          doc.getElementById("nino-error-summary").attr("href") mustBe "#nino"
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
