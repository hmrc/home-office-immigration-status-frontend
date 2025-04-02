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

package forms

import forms.helpers.FormHelper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.data.{Form, FormError}

class FormHelperSpec extends AnyWordSpecLike with Matchers {

  val form: Form[(String, String)] = Form(tuple("myField1" -> nonEmptyText, "dateOfBirth" -> nonEmptyText))
  def testFormFill(myField1: String, dateOfBirth: String): Map[String, String] =
    Map("myField1" -> myField1, "dateOfBirth" -> dateOfBirth)

  "updateDateOfBirthErrors" should {
    "convert all dob errors to have the key be the day" in {
      val filledForm = form.bind(testFormFill("string", ""))
      FormHelper.updateDateOfBirthErrors(filledForm.errors) shouldBe List(
        FormError("dateOfBirth.day", List("error.required"))
      )
    }

    "not change non dob errors" in {
      val filledForm = form.bind(testFormFill("", "dob"))
      FormHelper.updateDateOfBirthErrors(filledForm.errors) shouldBe filledForm.errors
    }

    "change only dob errors in a mixture" in {
      val filledForm = form.bind(testFormFill("", ""))
      FormHelper.updateDateOfBirthErrors(filledForm.errors) shouldBe List(
        FormError("myField1", List("error.required")),
        FormError("dateOfBirth.day", List("error.required"))
      )
    }

  }

}
