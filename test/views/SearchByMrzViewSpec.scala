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

import forms.SearchByMRZForm
import models.MrzSearchFormModel
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.SearchByMrzView

class SearchByMrzViewSpec extends ViewSpec {

  private lazy val sut: SearchByMrzView = inject[SearchByMrzView]

  private val titleAndHeading: String = "Search by passport or ID card"

  private val mrzSearchFormModel: MrzSearchFormModel = MrzSearchFormModel(
    documentType = "PASSPORT",
    documentNumber = "123456",
    dateOfBirth = LocalDate.parse("1980-12-22"),
    nationality = "AFG"
  )

  private val validForm: Form[MrzSearchFormModel]   = inject[SearchByMRZForm].apply().fill(mrzSearchFormModel)
  private val invalidForm: Form[MrzSearchFormModel] = inject[SearchByMRZForm].apply().bind(Map("" -> ""))

  private def viewViaApply(form: Form[MrzSearchFormModel]): HtmlFormat.Appendable  = sut.apply(form)(request, messages)
  private def viewViaRender(form: Form[MrzSearchFormModel]): HtmlFormat.Appendable = sut.render(form, request, messages)
  private def viewViaF(form: Form[MrzSearchFormModel]): HtmlFormat.Appendable      = sut.f(form)(request, messages)

  "SearchByMrzView" when {
    def test(method: String, viewWithoutErrors: HtmlFormat.Appendable, viewWithErrors: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val docWithoutErrors: Document = asDocument(viewWithoutErrors)
        val docWithErrors: Document    = asDocument(viewWithErrors)
        "have the title and heading" in {
          assertElementHasText(docWithoutErrors, "title", s"$titleAndHeading - Check immigration status - GOV.UK")
          assertElementHasText(docWithoutErrors, "#mrz-search-title", titleAndHeading)
        }

        "have the search description paragraph content" in {
          assertElementHasText(
            docWithoutErrors,
            "#search-description",
            "Enter all the information to search for the customer by passport or ID card. Or you can search by National Insurance number."
          )
        }

        "have the alternative search link" in {
          assertElementHasText(docWithoutErrors, "#alt-search-by-nino", "search by National Insurance number")
          docWithoutErrors
            .getElementById("alt-search-by-nino")
            .attr("href") mustBe "/check-immigration-status/search-by-nino?clearForm=true"
        }

        "have the documentType, documentNumber, search button and nationality select" in {
          assertRenderedById(docWithoutErrors, "documentType")
          assertRenderedById(docWithoutErrors, "documentNumber")
          assertRenderedById(docWithoutErrors, "search-button")
          assertRenderedById(docWithoutErrors, "nationality")
        }

        "have the identity component options" in {
          assertElementHasText(
            docWithoutErrors,
            "#documentType",
            "Passport European National Identity Card Biometric Residence Card Biometric Residence Permit"
          )
        }

        "have the dob input" in {
          docWithoutErrors.getElementById("dateOfBirth.day").attr("value") mustBe "22"
          docWithoutErrors.getElementById("dateOfBirth.month").attr("value") mustBe "12"
          docWithoutErrors.getElementById("dateOfBirth.year").attr("value") mustBe "1980"
        }

        "have the error summary" in {
          assertRenderedByClass(docWithErrors, "govuk-error-summary")
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(validForm), viewViaApply(invalidForm)),
      (".render", viewViaRender(validForm), viewViaRender(invalidForm)),
      (".f", viewViaF(validForm), viewViaF(invalidForm))
    )

    input.foreach(args => (test _).tupled(args))
  }
}
