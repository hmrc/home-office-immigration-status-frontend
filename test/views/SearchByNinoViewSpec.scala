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

package views

import java.time.LocalDate
import forms.SearchByNinoForm
import models.NinoSearchFormModel
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.NinoGenerator.generateNino
import views.html.SearchByNinoView

class SearchByNinoViewSpec extends ViewSpec {

  private lazy val sut: SearchByNinoView = inject[SearchByNinoView]

  private val ninoSearchFormModel: NinoSearchFormModel = NinoSearchFormModel(
    nino = generateNino,
    givenName = "Josh",
    familyName = "Walker",
    dateOfBirth = LocalDate.parse("1990-10-11")
  )

  private val form: Form[NinoSearchFormModel] = new SearchByNinoForm()().fill(ninoSearchFormModel)

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(form)(request, messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(form, request, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(form)(request, messages)

  "SearchByNinoView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the title and heading" in {
          assertElementHasText(doc, "title", "Search by National Insurance number - Check immigration status - GOV.UK")
          assertElementHasText(doc, "#nino-search-title", "Search by National Insurance number")
        }

        "have the search description paragraph content" in {
          assertElementHasText(
            doc,
            "#search-description",
            "Enter all the information to search for the customer by National Insurance number. Or you can search by passport or ID card."
          )
        }

        "have the alternative search link" in {
          assertElementHasText(doc, "#alt-search-by-mrz", "search by passport or ID card")
          doc
            .getElementById("alt-search-by-mrz")
            .attr("href") mustBe "/check-immigration-status/search-by-passport?clearForm=true"
        }

        "have the nino input" in {
          doc.getElementById("nino").attr("value") mustBe ninoSearchFormModel.nino.nino
        }

        "have the givenName input" in {
          doc.getElementById("givenName").attr("value") mustBe "Josh"
        }

        "have the familyName input" in {
          doc.getElementById("familyName").attr("value") mustBe "Walker"
        }

        "have the dob input" in {
          doc.getElementById("dateOfBirth.day").attr("value") mustBe "11"
          doc.getElementById("dateOfBirth.month").attr("value") mustBe "10"
          doc.getElementById("dateOfBirth.year").attr("value") mustBe "1990"
        }

        "have the search button" in {
          assertElementHasText(doc, "#search-button", "Search")
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
