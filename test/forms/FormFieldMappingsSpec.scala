/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.data.Mapping
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.{Form, FormError}
import play.api.data.Forms.single
import play.api.data.validation.Invalid

class FormFieldMappingsSpec
    extends AnyWordSpecLike with Matchers with OptionValues with FormFieldMappings with ScalaCheckDrivenPropertyChecks {

  def validateName(errorName: String, len: Int): Mapping[String] = validName(fieldName = errorName, minLenInc = len)
  val invalid: Invalid = Invalid("error.bar.invalid-format")

  def form(name: String, min: Int) = Form(single(name -> validateName(name, min)))
  def testFormFill(map: String) = Map("foo" -> map)

  "FormFieldMappings" should {
    "checks emptiness" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
    }

    "validate name" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
      form("foo", 1).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))

      form("1", 2).bind(testFormFill("")).errors shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("1", 1).bind(testFormFill("")).errors shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("a1", 2).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("a1", 1).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("1a", 2).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))
      form("1a", 1).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))

      form("Artur", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("Artur", List("error.Artur.required"), Seq()))
      form("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", List("error.ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž.required"), Seq()))

    }

    val intGen = Gen.numStr.suchThat(str => str.length > 0).map(_.take(9))

    "isInt" should {
      "return true for a number" in {
        forAll(intGen)(n => isInt(n) shouldBe true)
      }

      "return false for a non-numeric string" in {
        forAll(Gen.alphaStr.suchThat(_.length > 0))(n => isInt(n) shouldBe false)
      }
    }

    "isNotZero" should {
      "return true for a number over zero" in {
        forAll(intGen.suchThat(_.toInt > 0))(n => isNotZero(n.toInt) shouldBe true)
      }

      "return false for zero" in {
        isNotZero(0) == false
      }
    }

    "dateComponent" should {

      def form(min: Int) = Form(single("myField" -> dateComponent("myField", min)))
      def testFormFill(day: String) = Map("myField" -> day)

      "checks emptiness" in {
        form(0).bind(testFormFill("")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.required")))
      }

      "check that the string is an int" in {
        forAll(Gen.alphaStr.suchThat(_.length > 0))(str =>
          form(0).bind(testFormFill(str)).errors shouldBe List(
            FormError("myField", List("error.dateOfBirth.myField.invalid"))))
      }

      "check that the string is not zero" in {
        form(0).bind(testFormFill("0")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.zero")))
      }

      "check that the string meets a minimum value" in {
        form(2).bind(testFormFill("1")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.min"), Seq(2)))
      }

      "check that the string is a minimum value for 4 digits" in {
        form(1000).bind(testFormFill("999")).errors shouldBe List(
          FormError("myField", List("error.dateOfBirth.myField.min"), Seq(1000)))
      }

      "not return an error where the field is valid" in {
        form(1000).bind(testFormFill("1000")).errors shouldBe Nil
      }
    }

  }
}
