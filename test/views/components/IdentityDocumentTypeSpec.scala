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

package views.components

import forms.SearchByMRZForm
import models.MrzSearch
import org.jsoup.nodes.Document
import play.api.data.Forms.mapping
import play.api.data.format.Formats.*
import play.api.data.*
import play.twirl.api.HtmlFormat
import views.ViewSpec
import views.html.components.identityDocumentType

import scala.jdk.CollectionConverters.*

class IdentityDocumentTypeSpec extends ViewSpec {

  private val sut: identityDocumentType = inject[identityDocumentType]

  private val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  private val emptyForm: Form[String] = testForm.bind(Map.empty[String, String])

  private def viewViaApply(form: Form[String]): HtmlFormat.Appendable  = sut.apply(form)(messages)
  private def viewViaRender(form: Form[String]): HtmlFormat.Appendable = sut.render(form, messages)
  private def viewViaF(form: Form[String]): HtmlFormat.Appendable      = sut.f(form)(messages)

  "IdentityDocumentType" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have all 4 options, in the correct order" in {
          val options = doc.select("option").asScala.toList.map(option => option.attr("value"))

          options mustBe MrzSearch.AllowedDocumentTypes
        }

        "default select passport" when {
          "no option previously selected" in {
            val optionsWithSelected: List[(String, Boolean)] =
              doc.select("option").asScala.toList.map(option => (option.attr("value"), option.hasAttr("selected")))

            optionsWithSelected.headOption mustBe Some(("PASSPORT", true))
            optionsWithSelected.tail.foreach { case (option, selected) =>
              withClue(s"$option was selected when it should not be.") {
                selected mustBe false
              }
            }
          }
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(emptyForm)),
      (".render", viewViaRender(emptyForm)),
      (".f", viewViaF(emptyForm))
    )
    input.foreach(args => test.tupled(args))

    ".apply" must {
      "preselect the selected" when {
        MrzSearch.AllowedDocumentTypes
          .foreach { selected =>
            s"selected is $selected" in {
              val doc: Document = asDocument(viewViaApply(testForm.bind(Map("documentType" -> selected))))

              val optionsWithSelected: List[(String, Boolean)] =
                doc.select("option").asScala.toList.map(option => (option.attr("value"), option.hasAttr("selected")))

              optionsWithSelected.find(_._2) mustBe Some((selected, true))
              optionsWithSelected.filterNot(_._2).length mustBe MrzSearch.AllowedDocumentTypes.length - 1
            }
          }
      }
    }
  }
}
