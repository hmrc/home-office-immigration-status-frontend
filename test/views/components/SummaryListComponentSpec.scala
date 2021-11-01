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

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import viewmodels.RowViewModel
import views.ViewSpec
import views.html.components.SummaryList

class SummaryListComponentSpec extends ViewSpec {

  val sut: SummaryList = inject[SummaryList]

  "SummaryList" must {
    "have a description list" in {
      val doc: Document = asDocument(sut("the-list-id", Nil)(messages))

      val list: Element = doc.getElementById("the-list-id")
      assert(list.hasClass("govuk-summary-list"))
    }

    def mkRow(i: Int) = RowViewModel(i.toString, s"message.$i", s"data$i")

    "list all elements passed as 1/3 gov uk rows" in {
      val doc: Document = asDocument(sut("the-list-id", Seq(1, 2, 3).map(mkRow))(messages))

      val list: Element = doc.getElementById("the-list-id")
      Seq(1, 2, 3).foreach { i =>
        val row: Elements = list.select(s".govuk-summary-list__row:nth-child($i)")
        assertOneThirdRow(row, s"message.$i", s"data$i", i.toString)
      }
    }
  }
}
