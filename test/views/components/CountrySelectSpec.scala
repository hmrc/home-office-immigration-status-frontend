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

package views.components

import config.Countries
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Forms.mapping
import play.api.data.format.Formats._
import play.api.data.{Form, Forms}
import views.ViewSpec
import views.html.components.CountrySelect

import scala.jdk.CollectionConverters._

class CountrySelectSpec extends ViewSpec {

  val sut: CountrySelect   = inject[CountrySelect]
  val countries: Countries = inject[Countries]

  val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  val emptyForm: Form[String]  = testForm.bind(Map.empty[String, String])
  val filledForm: Form[String] = testForm.bind(Map("nationality" -> "GBR"))

  val doc: Document          = asDocument(sut(emptyForm)(messages))
  val countrySelect: Element = doc.getElementById("nationality")

  "form group" must {
    "have the autocomplete-wrapper class" in {
      val formGroup = doc.select(".govuk-form-group")
      assert(formGroup.hasClass("autocomplete-wrapper"))
    }
  }

  "CountrySelect" must {
    "have a label" in {
      val label = doc.select(".govuk-label")
      label.text() mustBe messages("lookup.nationality.label")
    }

    "have hint text" in {
      val nationalityHint: Element = doc.getElementById("nationality-hint")
      nationalityHint.text() mustBe messages("lookup.nationality.hint")
    }

  }

  "nationality" must {
    "have the data-all-countries attribute" in {
      val countrySelect: Option[Elements] = Option(doc.select("nationality[data-all-countries]"))
      countrySelect mustBe defined
    }

    "have an item for each country" in {
      val options             = doc.select("option")
      val optionTuples        = options.asScala.toList.map(option => (option.attr("value"), option.text()))
      val countryConfigTuples = countries.countries.map(country => country.alpha3 -> country.name) :+ " " -> ""
      optionTuples must contain theSameElementsAs countryConfigTuples
    }

    "have the selected item set when it's passed in" in {
      val doc: Document = asDocument(sut(filledForm)(messages))
      val options       = doc.select("option[selected]").asScala.toList
      options.map(_.text()) must contain theSameElementsAs List("United Kingdom")
    }

    "have no selected item set when it's not passed in" in {
      val doc: Document = asDocument(sut(emptyForm)(messages))
      val options       = doc.select("option[selected]").asScala.toList
      options.map(_.text()) must contain theSameElementsAs Nil
    }
  }
}
