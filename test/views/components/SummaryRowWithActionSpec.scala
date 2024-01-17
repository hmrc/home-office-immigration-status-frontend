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

import java.time.LocalDate
import models.NinoSearchFormModel
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator.generateNino
import viewmodels.RowWithActionViewModel
import views.ViewSpec
import views.html.components.summaryRowWithAction

class SummaryRowWithActionSpec extends ViewSpec {

  private val nino: Nino = generateNino

  private val ninoSearchFormModel: NinoSearchFormModel = NinoSearchFormModel(
    nino = nino,
    givenName = "Josh",
    familyName = "Walker",
    dateOfBirth = LocalDate.parse("1990-02-01")
  )

  private val rowWithActionViewModel: RowWithActionViewModel = RowWithActionViewModel.apply(
    id = "nino",
    messageKey = "generic.nino",
    data = nino.nino,
    actionId = "change-nino",
    fieldId = "nino",
    spanMessageKey = "generic.nino",
    formModel = ninoSearchFormModel
  )

  private val viewViaApply: HtmlFormat.Appendable  = summaryRowWithAction.apply(rowWithActionViewModel)(messages)
  private val viewViaRender: HtmlFormat.Appendable = summaryRowWithAction.render(rowWithActionViewModel, messages)
  private val viewViaF: HtmlFormat.Appendable      = summaryRowWithAction.f(rowWithActionViewModel)(messages)

  "SummaryRowWithAction" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the name text in a description list" in {
          assertElementHasText(doc, ".govuk-summary-list__key", "National Insurance number")
        }

        "have the value text in a description list" in {
          assertElementHasText(doc, "#nino", nino.nino)
        }

        "have the link text in a description list" in {
          assertElementHasText(doc, "#change-nino", "Change National Insurance number")
        }

        "have the link in a description list" in {
          doc.getElementById("change-nino").attr("href") must endWith("/search-by-nino#nino")
        }

        "have the visually hidden text in a description list" in {
          assertElementHasText(doc, "span", "National Insurance number")
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
