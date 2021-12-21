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
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import scala.util.Try

class FormFieldMappingsSpec
    extends AnyWordSpecLike with Matchers with OptionValues with FormFieldMappings with ScalaCheckDrivenPropertyChecks {

  val validateName: Constraint[String] = validName(fieldName = "bar", minLenInc = 0)
  val invalid: Invalid = Invalid("error.bar.invalid-format")

  "FormFieldMappings" should {
    "validate name" in {
      validName(fieldName = "foo", minLenInc = 2)("a") shouldBe Invalid("error.foo.invalid-format")
      validName(fieldName = "foo", minLenInc = 1)("a") shouldBe Valid
      validateName("1") shouldBe invalid
      validateName("1a") shouldBe invalid
      validateName("a1") shouldBe invalid
      validateName("a1") shouldBe invalid
      validateName("Artur") shouldBe Valid
      validateName("Art ur") shouldBe Valid
      validateName("Art-ur") shouldBe Valid
      validateName("Art'ur") shouldBe Valid
      validateName("Art'ur") shouldBe Valid
      validateName("Art2ur") shouldBe invalid
      validateName("Art_ur") shouldBe invalid
      validateName("$Artur") shouldBe invalid
      validateName("@Artur") shouldBe invalid
      validateName("Ar#tur") shouldBe invalid
      validateName("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž") shouldBe Valid
    }

    val intGen = Gen.numStr.suchThat(str => str.length > 0).map(_.take(9))

    "isInt" should {
      "return true for a number" in {
        forAll(intGen)(n => isInt(n) == true)
      }

      "return false for a non-numeric string" in {
        forAll(Gen.alphaStr.suchThat(_.length > 0))(n => isInt(n) == true)
      }
    }

    "isNotZero" should {
      "return true for a number over zero" in {
        forAll(intGen.suchThat(_.toInt > 0))(n => isNotZero(n) == true)
      }

      "return false for zero" in {
        isNotZero("0") == false
      }
    }

    "validateNonEmptyInt" should {
      "checks emptiness" in {
        validateNonEmptyInt("myField", None)("") shouldBe Invalid(ValidationError("error.dateOfBirth.myField.required"))
      }

      "check that the string is an int" in {
        forAll(Gen.alphaStr.suchThat(_.length > 0))(validateNonEmptyInt("myField", None)(_) shouldBe Invalid(
          ValidationError("error.dateOfBirth.myField.invalid")))
      }

      "check that the string is not zero" in {
        validateNonEmptyInt("myField", None)("0") shouldBe Invalid(
          ValidationError("error.dateOfBirth.myField.required"))
      }

      "check that the string is a minimum number of characters" in {
        validateNonEmptyInt("myField", Some(2))("1") shouldBe Invalid(
          ValidationError("error.dateOfBirth.myField.length", 2))
      }
    }

  }
}
