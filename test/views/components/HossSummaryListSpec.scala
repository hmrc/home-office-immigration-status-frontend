/*
 * Copyright 2026 HM Revenue & Customs
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

import org.jsoup.nodes._
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import viewmodels.RowViewModel
import views.ViewSpec
import views.html.components.HossSummaryList

class HossSummaryListSpec extends ViewSpec {

  private val sut: HossSummaryList = inject[HossSummaryList]

  private def mkRow(i: Int): RowViewModel = RowViewModel(i.toString, s"message.$i", s"data$i")

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply("the-list-id", Seq(1, 2, 3).map(mkRow))(messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render("the-list-id", Seq(1, 2, 3).map(mkRow), messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f("the-list-id", Seq(1, 2, 3).map(mkRow))(messages)

  "HossSummaryList" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the description list" in {
          assertElementHasText(doc, ".govuk-summary-list", "message.1 data1 message.2 data2 message.3 data3")
        }

        "list all elements passed as 1/3 gov uk rows" in {
          val list: Element = doc.getElementById("the-list-id")
          Seq(1, 2, 3).foreach { i =>
            val row: Elements = list.select(s".govuk-summary-list__row:nth-child($i)")
            assertCustomWidthRow(row, s"message.$i", s"data$i", i.toString, "third")
          }
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
