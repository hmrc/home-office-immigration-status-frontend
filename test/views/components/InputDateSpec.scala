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

import forms.SearchByMRZForm
import models.MrzSearchFormModel
import org.jsoup.nodes.Document
import play.api.data.Forms.mapping
import play.api.data.format.Formats._
import play.api.data._
import play.twirl.api._
import views.ViewSpec
import views.html.components.inputDate

class InputDateSpec extends ViewSpec {

  private val sut: inputDate = inject[inputDate]

  private val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  private val emptyForm: Form[String] = testForm.bind(Map.empty[String, String])
  private val invalidForm: Form[MrzSearchFormModel] = inject[SearchByMRZForm]
    .apply()
    .bind(
      Map(
        "dateOfBirth.day"   -> "date",
        "dateOfBirth.month" -> "month",
        "dateOfBirth.year"  -> "year"
      )
    )

  private def viewViaApply(form: Form[_]): HtmlFormat.Appendable = sut.apply(
    form = form,
    legendContent = messages("some.legend.content"),
    legendClasses = "govuk-label",
    id = "dateOfBirth",
    hintMessage = Some(messages("lookup.dateOfBirth.hint")),
    hintHtml = Some(Html(messages("lookup.dateOfBirth.hint")))
  )(messages)

  private def viewViaRender(form: Form[_]): HtmlFormat.Appendable = sut.render(
    form = form,
    legendContent = messages("some.legend.content"),
    legendClasses = "govuk-label",
    id = "dateOfBirth",
    hintMessage = Some(messages("lookup.dateOfBirth.hint")),
    hintHtml = Some(Html(messages("lookup.dateOfBirth.hint"))),
    legendAsPageHeading = false,
    messages = messages
  )

  private def viewViaF(form: Form[_]): HtmlFormat.Appendable = sut.f(
    form,
    messages("some.legend.content"),
    "govuk-label",
    "dateOfBirth",
    Some(messages("lookup.dateOfBirth.hint")),
    Some(Html(messages("lookup.dateOfBirth.hint"))),
    false
  )(messages)

  "InputDate" when {
    def test(
      method: String,
      viewWithEmptyForm: HtmlFormat.Appendable,
      viewWithInvalidForm: HtmlFormat.Appendable
    ): Unit =
      s"$method" must {
        val docWithEmptyForm: Document   = asDocument(viewWithEmptyForm)
        val docWithInvalidForm: Document = asDocument(viewWithInvalidForm)
        "have the date of birth content" in {
          assertElementHasText(docWithEmptyForm, "#dateOfBirth", "Day Month Year")
        }

        "have the legend content" in {
          docWithEmptyForm.getElementsByTag("legend").attr("class") mustBe "govuk-fieldset__legend govuk-label"
          assertElementHasText(docWithEmptyForm, "legend", "some.legend.content")
        }

        "have the day content" in {
          assertRenderedById(docWithEmptyForm, "dateOfBirth.day")
          docWithEmptyForm.getElementsByAttributeValue("for", "dateOfBirth.day").text() mustBe "Day"
        }

        "have the month content" in {
          assertRenderedById(docWithEmptyForm, "dateOfBirth.month")
          docWithEmptyForm.getElementsByAttributeValue("for", "dateOfBirth.month").text() mustBe "Month"
        }

        "have the year content" in {
          assertRenderedById(docWithEmptyForm, "dateOfBirth.year")
          docWithEmptyForm.getElementsByAttributeValue("for", "dateOfBirth.year").text() mustBe "Year"
        }

        "have the error class for day" in {
          docWithInvalidForm
            .getElementById("dateOfBirth.day")
            .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-2 govuk-input--error"
        }

        "have the error class for month" in {
          docWithInvalidForm
            .getElementById("dateOfBirth.month")
            .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-2 govuk-input--error"
        }

        "have the error class for year" in {
          docWithInvalidForm
            .getElementById("dateOfBirth.year")
            .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-4 govuk-input--error"
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(emptyForm), viewViaApply(invalidForm)),
      (".render", viewViaRender(emptyForm), viewViaRender(invalidForm)),
      (".f", viewViaF(emptyForm), viewViaF(invalidForm))
    )

    input.foreach(args => (test _).tupled(args))
  }
}
