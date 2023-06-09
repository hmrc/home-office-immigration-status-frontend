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

import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import viewmodels.RowViewModel
import views.ViewSpec
import views.html.components.summaryRow

class SummaryRowSpec extends ViewSpec {

  private val rowViewModel: RowViewModel = RowViewModel(
    id = "recourse-text",
    messageKey = "status-found.norecourse",
    data = "No"
  )

  private val viewViaApply: HtmlFormat.Appendable  = summaryRow.apply(rowViewModel)(messages)
  private val viewViaRender: HtmlFormat.Appendable = summaryRow.render(rowViewModel, messages)
  private val viewViaF: HtmlFormat.Appendable      = summaryRow.f(rowViewModel)(messages)

  "SummaryRow" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the name text in a description list" in {
          assertElementHasText(doc, ".govuk-summary-list__key", "Recourse to public funds")
        }

        "have the value text in a description list" in {
          assertElementHasText(doc, "#recourse-text", "No")
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
