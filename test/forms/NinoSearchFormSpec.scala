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

import models.NinoSearchFormModel
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.{Form, FormError}
import uk.gov.hmrc.domain.Nino
import org.scalacheck.Shrink
import utils.NinoGenerator

import java.time.LocalDate

class NinoSearchFormSpec extends PlaySpec with OptionValues with ScalaCheckDrivenPropertyChecks {

  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny

  val formProvider: StatusCheckByNinoFormProvider = new StatusCheckByNinoFormProvider()
  val form: Form[NinoSearchFormModel] = formProvider()

  val now: LocalDate = LocalDate.now()
  val tomorrow: LocalDate = now.plusDays(1)
  val yesterday: LocalDate = now.minusDays(1)
  val testNino = NinoGenerator.generateNino

  def input(
    year: String = yesterday.getYear.toString,
    month: String = yesterday.getMonthValue.toString,
    day: String = yesterday.getDayOfMonth.toString,
    familyName: String = "last",
    givenName: String = "first",
    nino: String = testNino.nino
  ) = Map(
    "dateOfBirth.year"  -> year,
    "dateOfBirth.month" -> month,
    "dateOfBirth.day"   -> day,
    "familyName"        -> familyName,
    "givenName"         -> givenName,
    "nino"              -> nino
  )

  val allowedSpecialChars = Gen.oneOf(formProvider.allowedNameCharacters)
  val validChar: Gen[String] =
    Gen.oneOf(Gen.alphaLowerChar, Gen.alphaUpperChar, allowedSpecialChars).map(_.toString).suchThat(_.trim.nonEmpty)

  val invalidCharString: Gen[String] = Gen.asciiPrintableStr
    .suchThat(_.trim.nonEmpty)
    .suchThat(_.exists(c => !c.isLetter && !formProvider.allowedNameCharacters.contains(c)))
    .suchThat(_.trim.nonEmpty)

  "form" must {
    "bind" when {
      "inputs are valid" in {
        val validInput = input()

        val out = NinoSearchFormModel(testNino, "first", "last", yesterday)

        val bound = form.bind(validInput)
        bound.errors mustBe Nil
        bound.value mustBe Some(out)
      }

      "givenName is one char" in {
        forAll(validChar) { name =>
          val validInput = input(givenName = name)

          val out = NinoSearchFormModel(testNino, name, "last", yesterday)
          val bound = form.bind(validInput)
          bound.value mustBe Some(out)
          bound.errors mustBe Nil
        }
      }

      "year is 2 digit, default add 19XX" in {
        val yearStr = for {
          y1 <- Gen.numChar
          y2 <- Gen.numChar
        } yield s"$y1$y2"
        forAll(yearStr.suchThat(_.length == 2)) { year =>
          val validInput = input(year = year)

          val out = NinoSearchFormModel(testNino, "first", "last", yesterday.withYear(("19" + year).toInt))
          val bound = form.bind(validInput)
          bound.errors mustBe Nil
          bound.value mustBe Some(out)
        }
      }
    }

    "fail to bind" when {
      "dob is today" in {
        val invalidInput = input(day = now.getDayOfMonth.toString)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("dateOfBirth", List("error.dateOfBirth.invalid-format")))
      }
      "dob is future" in {
        val invalidInput = input(day = tomorrow.getDayOfMonth.toString)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("dateOfBirth", List("error.dateOfBirth.invalid-format")))
      }

      "givenName is empty" in {
        val invalidInput = input(givenName = "")

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("givenName", List("error.givenName.required")))
      }

      "givenName contains invalid chars" in {
        forAll(invalidCharString) { name =>
          val invalidInput = input(givenName = name)

          form.bind(invalidInput).value must not be defined
          form.bind(invalidInput).errors mustBe List(FormError("givenName", List("error.givenName.invalid-format")))
        }
      }

      "familyName is empty" in {
        val invalidInput = input(familyName = "")

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("familyName", List("error.familyName.required")))
      }

      "familyName is too short" in {
        forAll(validChar) { name =>
          val invalidInput = input(familyName = name)

          form.bind(invalidInput).value must not be defined
          form.bind(invalidInput).errors mustBe List(FormError("familyName", List("error.familyName.invalid-format")))
        }
      }

      "familyName contains invalid chars" in {
        forAll(invalidCharString) { name =>
          val invalidInput = input(familyName = name)

          form.bind(invalidInput).value must not be defined
          form.bind(invalidInput).errors mustBe List(FormError("familyName", List("error.familyName.invalid-format")))
        }
      }

      "nino is empty" in {
        val invalidInput = input(nino = "")

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("nino", List("error.nino.required")))
      }
      "nino is invalid" in {
        forAll(invalidCharString) { nino =>
          val invalidInput = input(nino = nino)

          form.bind(invalidInput).value must not be defined
          form.bind(invalidInput).errors mustBe List(FormError("nino", List("error.nino.invalid-format")))
        }
      }
    }
  }
}