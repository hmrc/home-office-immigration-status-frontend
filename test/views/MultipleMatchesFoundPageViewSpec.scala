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
import views.html.MultipleMatchesFoundPage

class MultipleMatchesFoundPageViewSpec extends ViewSpec {

  private lazy val sut: MultipleMatchesFoundPage = inject[MultipleMatchesFoundPage]

  private val ninoSearchFormModel: NinoSearchFormModel =
    NinoSearchFormModel(generateNino, "Pan", "Walker", LocalDate.now())
  private val mrzSearchFormModel: MrzSearchFormModel =
    MrzSearchFormModel("PASSPORT", "123456", LocalDate.parse("2001-01-31"), "USA")

  private def viewViaApply(query: SearchFormModel): HtmlFormat.Appendable  = sut.apply(query)(request, messages)
  private def viewViaRender(query: SearchFormModel): HtmlFormat.Appendable = sut.render(query, request, messages)
  private def viewViaF(query: SearchFormModel): HtmlFormat.Appendable      = sut.f(query)(request, messages)

  "MultipleMatchesFoundPageView" when {
    def test(method: String, ninoSearchView: HtmlFormat.Appendable, mrzSearchView: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val ninoDoc: Document = asDocument(ninoSearchView)
        val mrzDoc: Document  = asDocument(mrzSearchView)
        "have the title and heading" in {
          assertElementHasText(mrzDoc, "title", "A unique match has not been found - Check immigration status - GOV.UK")
          assertElementHasText(mrzDoc, "#status-check-failure-conflict-title", "A unique match has not been found")
        }

        "have the personal details heading" in {
          assertElementHasText(mrzDoc, "#personal-details", "Customer details")
        }

        "have the mrz alt link for nino doc" in {
          assertElementHasText(
            ninoDoc,
            "#alternate-search",
            "You can change the customer’s details you have entered or search by passport or ID card."
          )
          assertElementHasText(ninoDoc, "#alt-search-by-mrz", "search by passport or ID card")
        }

        "have the nino alt link for mrz doc" in {
          assertElementHasText(
            mrzDoc,
            "#alternate-search",
            "You can change the customer’s details you have entered or search by National Insurance number."
          )
          assertElementHasText(mrzDoc, "#alt-search-by-nino", "search by National Insurance number")
        }

        "have the multiple label" in {
          assertElementHasText(
            mrzDoc,
            "#multiplelabel",
            "This is because there are multiple matches with these details."
          )
        }

        "have the show and change query section" in {
          assertRenderedById(mrzDoc, "inputted-data")
        }

        "have the search again button" in {
          assertElementHasText(mrzDoc, "#search-again-button", "Search again")
          mrzDoc.getElementById("search-again-button").attr("href") mustBe "/check-immigration-status"
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
