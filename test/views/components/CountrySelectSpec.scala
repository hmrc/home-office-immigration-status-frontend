/*
 * Copyright 2025 HM Revenue & Customs
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
import org.jsoup.nodes._
import org.jsoup.select.Elements
import play.api.data.Forms.mapping
import play.api.data.format.Formats._
import play.api.data._
import play.twirl.api.HtmlFormat
import views.ViewSpec
import views.html.components.CountrySelect

import scala.jdk.CollectionConverters._

class CountrySelectSpec extends ViewSpec {

  private val sut: CountrySelect   = inject[CountrySelect]
  private val countries: Countries = inject[Countries]

  private val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  private val emptyForm: Form[String]  = testForm.bind(Map.empty[String, String])
  private val filledForm: Form[String] = testForm.bind(Map("nationality" -> "GBR"))

  private def viewViaApply(form: Form[String]): HtmlFormat.Appendable  = sut.apply(form)(messages)
  private def viewViaRender(form: Form[String]): HtmlFormat.Appendable = sut.render(form, messages)
  private def viewViaF(form: Form[String]): HtmlFormat.Appendable      = sut.f(form)(messages)

  "CountrySelect" when {
    def test(
      method: String,
      viewWithEmptyForm: HtmlFormat.Appendable,
      viewWithFilledForm: HtmlFormat.Appendable
    ): Unit =
      s"$method" must {
        val docWithEmptyForm: Document  = asDocument(viewWithEmptyForm)
        val docWithFilledForm: Document = asDocument(viewWithFilledForm)
        "have the label text" in {
          assertElementHasText(docWithEmptyForm, ".govuk-label", "Country of nationality")
        }

        "have the hint text" in {
          assertElementHasText(docWithEmptyForm, "#nationality-hint", "For example, France.")
        }

        "have the autocomplete-wrapper class" in {
          assertRenderedByClass(docWithEmptyForm, "autocomplete-wrapper")
        }

        "have the data-all-countries attribute" in {
          val countrySelect: Option[Elements] = Option(docWithEmptyForm.select("nationality[data-all-countries]"))
          countrySelect mustBe defined
        }

        "have an item for each country" in {
          val options: Elements = docWithEmptyForm.select("option")
          val optionTuples: List[(String, String)] =
            options.asScala.toList.map(option => (option.attr("value"), option.text()))
          val countryConfigTuples: Seq[(String, String)] =
            countries.countries.map(country => country.alpha3 -> country.name) :+ " " -> ""
          optionTuples must contain theSameElementsAs countryConfigTuples
        }

        "have the selected item set when it is passed" in {
          val options: List[Element] = docWithFilledForm.select("option[selected]").asScala.toList
          options.map(_.text()) must contain theSameElementsAs List("United Kingdom")
        }

        "have no selected item set when it is not passed" in {
          val options: List[Element] = docWithEmptyForm.select("option[selected]").asScala.toList
          options.map(_.text()) must contain theSameElementsAs Nil
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(emptyForm), viewViaApply(filledForm)),
      (".render", viewViaRender(emptyForm), viewViaRender(filledForm)),
      (".f", viewViaF(emptyForm), viewViaF(filledForm))
    )

    input.foreach(args => test.tupled(args))
  }
}
