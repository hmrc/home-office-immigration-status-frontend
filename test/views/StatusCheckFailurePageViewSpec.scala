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
import views.html.StatusCheckFailurePage

class StatusCheckFailurePageViewSpec extends ViewSpec {

  private lazy val sut: StatusCheckFailurePage = inject[StatusCheckFailurePage]

  private val bulletPointContent: List[(Int, String)] = List(
    (1, "the details you have entered are incorrect"),
    (2, "the Home Office has not updated the customer’s information")
  )

  private val ninoSearchFormModel: NinoSearchFormModel =
    NinoSearchFormModel(generateNino, "Pan", "Walker", LocalDate.now())
  private val mrzSearchFormModel: MrzSearchFormModel = MrzSearchFormModel("PASSPORT", "123456", LocalDate.now(), "USA")

  private def viewViaApply(query: SearchFormModel): HtmlFormat.Appendable  = sut.apply(query)(request, messages)
  private def viewViaRender(query: SearchFormModel): HtmlFormat.Appendable = sut.render(query, request, messages)
  private def viewViaF(query: SearchFormModel): HtmlFormat.Appendable      = sut.f(query)(request, messages)

  "StatusCheckFailurePageView" when {
    def test(method: String, ninoSearchView: HtmlFormat.Appendable, mrzSearchView: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val ninoDoc: Document = asDocument(ninoSearchView)
        val mrzDoc: Document  = asDocument(mrzSearchView)
        "have the title and heading" in {
          assertElementHasText(
            ninoDoc,
            "title",
            "The details you entered do not match Home Office records - Check immigration status - GOV.UK"
          )
          assertElementHasText(
            ninoDoc,
            "#status-check-failure-title",
            "The details you entered do not match Home Office records"
          )
        }

        "have the paragraph content" in {
          assertElementHasText(ninoDoc, ".govuk-body", "This may be because:")
        }

        "have the bullet point content" when {
          bulletPointContent.foreach { case (number, message) =>
            s"it is bullet point number $number" in {
              assertElementHasText(ninoDoc, s"#item$number", message)
            }
          }
        }

        "have the personal details heading" in {
          assertElementHasText(ninoDoc, "#personal-details", "Customer details")
        }

        "have the mrz alt link for nino doc" in {
          assertElementHasText(
            ninoDoc,
            "#alt-search-by-mrz",
            "search by passport or ID card"
          )
          assertElementHasText(ninoDoc, "#alt-search-by-mrz", "search by passport or ID card")
        }

        "have the nino alt link for mrz doc" in {
          assertElementHasText(
            mrzDoc,
            "#alt-search-by-nino",
            "search by National Insurance number"
          )
          assertElementHasText(mrzDoc, "#alt-search-by-nino", "search by National Insurance number")
        }

        "have the search again button" in {
          assertElementHasText(ninoDoc, "#search-again-button", "Search again")
          ninoDoc.getElementById("search-again-button").attr("href") mustBe "/check-immigration-status"
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(ninoSearchFormModel), viewViaApply(mrzSearchFormModel)),
      (".render", viewViaRender(ninoSearchFormModel), viewViaRender(mrzSearchFormModel)),
      (".f", viewViaF(ninoSearchFormModel), viewViaF(mrzSearchFormModel))
    )

    input.foreach(args => (test _).tupled(args))
  }
}
