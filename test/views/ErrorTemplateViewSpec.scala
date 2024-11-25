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

package views

import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.html.error_template

class ErrorTemplateViewSpec extends ViewSpec {

  private val sut: error_template = inject[error_template]

  private val pageTitle: String = "Sorry, there is a problem with the service"
  private val heading: String   = "Sorry, there is a problem with the service"
  private val message: String   = "internal.error.500.message"

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(pageTitle, heading, message)(request, messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(pageTitle, heading, message, None, request, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(pageTitle, heading, message, None)(request, messages)

  "ErrorTemplateView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the title and heading" in {
          assertElementHasText(doc, "title", s"$pageTitle - Check immigration status - GOV.UK")
          assertElementHasText(doc, "#title", heading)
        }

        "have the paragraph content" in {
          assertElementHasText(doc, ".govuk-body", "Contact the Helpline.")
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
