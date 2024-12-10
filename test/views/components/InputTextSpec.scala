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

import forms.SearchByNinoForm
import models.NinoSearchFormModel
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api._
import views.ViewSpec
import views.html.components.inputText

class InputTextSpec extends ViewSpec {

  private val id: String    = "nino"
  private val name: String  = "nino"
  private val label: String = "lookup.nino.label"

  private val form: Form[NinoSearchFormModel] = new SearchByNinoForm()()

  private val sut: inputText = inject[inputText]

  private val viewViaApply: HtmlFormat.Appendable = sut.apply(
    form = form,
    id = id,
    name = name,
    label = label,
    isPageHeading = true,
    hint = Some(Html(messages("lookup.nino.hint"))),
    isTelephone = true
  )(messages)

  private val viewViaRender: HtmlFormat.Appendable = sut.render(
    form = form,
    id = id,
    name = name,
    label = label,
    isPageHeading = true,
    headingMessageArgs = Seq(),
    hint = Some(Html(messages("lookup.nino.hint"))),
    classes = None,
    stripWhitespace = false,
    inputType = "text",
    isTelephone = true,
    labelClasses = None,
    messages = messages
  )

  private val viewViaF: HtmlFormat.Appendable = sut.f(
    form,
    id,
    name,
    label,
    true,
    Seq(),
    Some(Html(messages("lookup.nino.hint"))),
    None,
    false,
    "text",
    true,
    None
  )(messages)

  "InputText" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the label text" in {
          assertElementHasText(doc, ".govuk-label", "National Insurance number")
        }

        "have the hint text" in {
          assertElementHasText(doc, s"#$id-hint", "For example, ‘QQ123456C’.")
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
