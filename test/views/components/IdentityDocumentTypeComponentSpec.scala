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
import play.api.data.{Form, Forms}
import play.api.data.Forms.mapping
import views.ViewSpec
import views.html.components.identityDocumentType
import play.api.data.format.Formats._

import scala.collection.JavaConverters.asScalaBufferConverter

class IdentityDocumentTypeComponentSpec extends ViewSpec {

  val sut: identityDocumentType = inject[identityDocumentType]

  val testForm: Form[String] = Form[String] {
    mapping("documentType" -> Forms.of[String])(identity)(Some.apply)
  }

  "apply" must {
    val emptyForm = testForm.bind(Map.empty[String, String])
    "have all 4 options, in the correct order" in {
      val doc = asDocument(sut(emptyForm)(messages))
      val options = doc.select("option").asScala.toList.map(option => (option.attr("value")))

      options mustBe SearchByMRZForm.AllowedDocumentTypes
    }

    "default select passport" when {
      "No option previously selected" in {
        val doc = asDocument(sut(emptyForm)(messages))

        val optionsWithSelected =
          doc.select("option").asScala.toList.map(option => (option.attr("value"), option.hasAttr("selected")))

        optionsWithSelected.headOption mustBe Some(("PASSPORT", true))
        optionsWithSelected.tail.foreach {
          case (option, selected) =>
            withClue(s"$option was selected when it shouldnt be.") {
              selected mustBe false
            }
        }
      }
    }

    "preselect the selected" when {
      SearchByMRZForm.AllowedDocumentTypes
        .foreach { selected =>
          s"selected is $selected" in {
            val doc = asDocument(sut(testForm.bind(Map("documentType" -> selected)))(messages))

            val optionsWithSelected =
              doc.select("option").asScala.toList.map(option => (option.attr("value"), option.hasAttr("selected")))

            optionsWithSelected.find(_._2) mustBe Some((selected, true))
            optionsWithSelected.filterNot(_._2).length mustBe SearchByMRZForm.AllowedDocumentTypes.length - 1
          }
        }
    }
  }

}
