/*
 * Copyright 2022 HM Revenue & Customs
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
import org.jsoup.nodes.{Document, Element}
import play.api.data.Forms.mapping
import play.api.data.format.Formats._
import play.api.data.{Form, Forms}
import play.api.libs.json.JsNull
import views.ViewSpec
import views.html.components.inputDate

class InputDateSpec extends ViewSpec {

  val sut: inputDate = inject[inputDate]

  val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  val emptyForm: Form[String] = testForm.bind(Map.empty[String, String])

  def renderDocument(form: Form[_]): Document =
    asDocument(
      sut(
        form,
        id = "dateOfBirth",
        legendClasses = "govuk-label",
        legendContent = messages("some.legend.content"),
        hintMessage = Some(messages("lookup.dateOfBirth.hint"))
      )(messages)
    )

  val doc                    = renderDocument(emptyForm)
  val countrySelect: Element = doc.getElementById("nationality")

  "inputDate" must {
    "assign the passed in id" in {
      assertRenderedById(doc, "dateOfBirth")
    }

    "assign the legend content" in {
      doc.getElementsByTag("legend").text() mustBe "some.legend.content"
    }

    "assign the legend classes" in {
      doc.getElementsByTag("legend").attr("class") mustBe "govuk-fieldset__legend govuk-label"
    }

    "render a day" in {
      assertRenderedById(doc, "dateOfBirth.day")
    }

    "render a month" in {
      assertRenderedById(doc, "dateOfBirth.month")
    }

    "render a year" in {
      assertRenderedById(doc, "dateOfBirth.year")
    }
  }

  val formWithErrors: Form[MrzSearchFormModel] = inject[SearchByMRZForm].apply().bind(JsNull, 200)
  lazy val docWithErrors: Document             = renderDocument(formWithErrors)

  "inputDate with errors" must {
    "assign day an error class" in {
      docWithErrors
        .getElementById("dateOfBirth.day")
        .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-2 govuk-input--error"
    }

    "assign month an error class" in {
      docWithErrors
        .getElementById("dateOfBirth.month")
        .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-2 govuk-input--error"
    }

    "assign year an error class" in {
      docWithErrors
        .getElementById("dateOfBirth.year")
        .attr("class") mustBe "govuk-input govuk-date-input__input govuk-input--width-4 govuk-input--error"
    }
  }

}
