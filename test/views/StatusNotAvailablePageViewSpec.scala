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

package views

import java.time.LocalDate

import models._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.NinoGenerator.generateNino
import views.html.StatusNotAvailablePage

class StatusNotAvailablePageViewSpec extends ViewSpec {

  private lazy val sut: StatusNotAvailablePage = inject[StatusNotAvailablePage]

  private val bulletPointContent: List[(Int, String)] = List(
    (1, "their status expired more than six months ago"),
    (2, "the Home Office does not have a record of any status for them")
  )

  private val ninoSearchFormModel: NinoSearchFormModel =
    NinoSearchFormModel(generateNino, "Josh", "Walker", LocalDate.now())
  private val result: StatusCheckResult = StatusCheckResult("Josh Walker", LocalDate.now(), "JPN", Nil)

  private val context: StatusNotAvailablePageContext = StatusNotAvailablePageContext(ninoSearchFormModel, result)

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(context)(request, messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(context, request, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(context)(request, messages)

  "StatusNotAvailablePageView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the title and heading" in {
          assertElementHasText(
            doc,
            "title",
            "Applicant does not have an active status - Check immigration status - GOV.UK"
          )
          assertElementHasText(doc, "#status-not-available-title", "Josh Walker does not have an active status")
        }

        "have the paragraph content" in {
          assertElementHasText(doc, "#not-available-paragraph", "This may be because:")
        }

        "have the bullet point content" when {
          bulletPointContent.foreach { case (number, message) =>
            s"it is bullet point number $number" in {
              assertElementHasText(doc, s"#item$number", message)
            }
          }
        }

        "have the summary list" in {
          assertRenderedById(doc, "notAvailablePersonalData")
        }

        "have the search again button" in {
          assertElementHasText(doc, "#search-again-button", "Search again")
          doc.getElementById("search-again-button").attr("href") mustBe "/check-immigration-status"
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
