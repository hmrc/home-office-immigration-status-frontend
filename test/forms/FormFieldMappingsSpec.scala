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

package forms

import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.Forms.single
import play.api.data.format.Formats.stringFormat
import play.api.data.validation.Invalid
import play.api.data.{Form, FormError, Forms, Mapping}

class FormFieldMappingsSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with FormFieldMappings
    with ScalaCheckDrivenPropertyChecks {

  def validateName(errorName: String, len: Int): Mapping[String] = validName(fieldName = errorName, minLenInc = len)
  val invalid: Invalid                                           = Invalid("error.bar.invalid-format")

  def form(name: String, min: Int): Form[String]     = Form(single(name -> validateName(name, min)))
  def testFormFill(map: String): Map[String, String] = Map("foo" -> map)

  "collateDOBErrors" should {
    def formWithErrors(errorKeyMessage: (String, String)*): Form[String] = {
      val someForm = Form("value" -> Forms.of[String]).discardingErrors
      errorKeyMessage.foldLeft(someForm)((form, error) => form.withError(error._1, error._2))
    }

    "group dob errors together" when {
      "there are 3 required errors" in {
        val testForm = formWithErrors(
          ("test.dateOfBirth", "dateOfBirth.pan.required"),
          ("other.error.dateOfBirth", "dateOfBirth.thing.required"),
          ("dateOfBirth", "prefix.dateOfBirth.another.required")
        )
        val result = collateDOBErrors(testForm)

        result.errors shouldBe Seq(FormError("dateOfBirth", "error.dateOfBirth.required"))
      }
      "there are less than 3 required errors" in {
        val testForm =
          formWithErrors(("test.dateOfBirth", "other.required"), ("other.error.dateOfBirth", "autre.required"))
        val result = collateDOBErrors(testForm)

        result.errors shouldBe Seq(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }
      "there are 2 mixed errors" in {
        val testForm =
          formWithErrors(("test.dateOfBirth", "other.required"), ("other.error.dateOfBirth", "autre.erreur"))
        val result = collateDOBErrors(testForm)

        result.errors shouldBe Seq(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }
      "there are 2 other errors" in {
        val testForm = formWithErrors(("test.dateOfBirth", "other.error"), ("other.error.dateOfBirth", "autre.erreur"))
        val result   = collateDOBErrors(testForm)

        result.errors shouldBe Seq(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }
    }

    "not change dob errors" when {
      "there is only one" in {
        val testForm = formWithErrors(("test.dateOfBirth", "dateOfBirt.pan.required"))
        val result   = collateDOBErrors(testForm)

        result shouldBe testForm
      }
    }

    "ignore non dob errors" in {
      val testForm =
        formWithErrors(("test.a.different.field", "other.required"), ("error.problem", "other.invalid-format"))
      val result = collateDOBErrors(testForm)

      result shouldBe testForm
    }
  }

  "FormFieldMappings" should {
    "checks emptiness" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
    }

    "validate name" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
      form("foo", 1).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))

      form("1", 2).bind(testFormFill("")).errors  shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("1", 1).bind(testFormFill("")).errors  shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("a1", 2).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("a1", 1).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("1a", 2).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))
      form("1a", 1).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))

      form("Artur", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("Artur", List("error.Artur.required"), Seq())
      )
      form("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", List("error.ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž.required"), Seq())
      )

    }

    val intGen: Gen[String] = Gen.numStr.suchThat(str => str.nonEmpty).map(_.take(9))

    "isInt" should {
      "return true for a number" in {
        forAll(intGen)(n => isInt(n) shouldBe true)
      }

      "return false for a non-numeric string" in {
        forAll(Gen.alphaStr.suchThat(_.nonEmpty))(n => isInt(n) shouldBe false)
      }
    }

    "isNotZero" should {
      "return true for a number over zero" in {
        forAll(intGen.suchThat(_.toInt > 0))(n => isNotZero(n.toInt) shouldBe true)
      }

      "return false for zero" in {
        isNotZero(0) shouldBe false
      }
    }

    "dateComponent" should {

      def form(max: Int, min: Int): Form[Int]            = Form(single("myField" -> dateComponent("myField", max, min)))
      def testFormFill(day: String): Map[String, String] = Map("myField" -> day)

      "checks emptiness" in {
        form(10, 0).bind(testFormFill("")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.required"))
        )
      }

      "check that the string is an int" in {
        forAll(Gen.alphaStr.suchThat(_.nonEmpty))(str =>
          form(10, 0).bind(testFormFill(str)).errors shouldBe List(
            FormError("myField", List("error.dateOfBirth.myField.invalid"))
          )
        )
      }

      "check that the string is not zero" in {
        form(10, 0).bind(testFormFill("0")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.zero"))
        )
      }

      "check that the string meets a minimum value" in {
        form(10, 2).bind(testFormFill("1")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.min"), Seq(2))
        )
      }

      "check that the string is a minimum value for 4 digits" in {
        form(2000, 1000).bind(testFormFill("999")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.min"), Seq(1000))
        )
      }

      "check that the string meets a maximum value" in {
        form(10, 2).bind(testFormFill("11")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.max"), Seq(10))
        )
      }

      "check that the string is a maximum value for 4 digits" in {
        form(2000, 1000).bind(testFormFill("2001")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.max"), Seq(2000))
        )
      }

      "not return an error where the field is valid" in {
        form(2000, 1000).bind(testFormFill("1000")).errors shouldBe Nil
      }
    }

  }
}
