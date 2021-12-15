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

import config.Countries
import models.MrzSearchFormModel
import org.scalacheck.{Gen, Shrink}
import org.scalatest.OptionValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.data.{Form, FormError}
import play.api.test.Injecting
import utils.NinoGenerator

import java.time.LocalDate

class SearchByMrzFormSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ScalaCheckDrivenPropertyChecks {

  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny

  lazy val formProvider: SearchByMRZForm = inject[SearchByMRZForm]
  lazy val countriesValues = inject[Countries].countries.map(_.alpha3)
  lazy val form: Form[MrzSearchFormModel] = formProvider()

  val now: LocalDate = LocalDate.now()
  val tomorrow: LocalDate = now.plusDays(1)
  val yesterday: LocalDate = now.minusDays(1)
  val testNino = NinoGenerator.generateNino

  def input(
    dateOfBirth: LocalDate = yesterday,
    nationality: String = "AFG",
    documentNumber: String = "docNumber",
    documentType: String = "PASSPORT"
  ) = Map(
    "dateOfBirth.year"  -> dateOfBirth.getYear.toString,
    "dateOfBirth.month" -> dateOfBirth.getMonthValue.toString,
    "dateOfBirth.day"   -> dateOfBirth.getDayOfMonth.toString,
    "nationality"       -> nationality,
    "documentNumber"    -> documentNumber,
    "documentType"      -> documentType
  )

  def inputYear(year: String) = Map(
    "dateOfBirth.year"  -> year,
    "dateOfBirth.month" -> yesterday.getMonthValue.toString,
    "dateOfBirth.day"   -> yesterday.getDayOfMonth.toString,
    "nationality"       -> "AFG",
    "documentNumber"    -> "docNumber",
    "documentType"      -> "PASSPORT"
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
        val validGen = for {
          docType <- Gen.oneOf(SearchByMRZForm.AllowedDocumentTypes)
          docNum <- Gen
                     .listOfN(
                       SearchByMRZForm.DocumentNumberMaxLength,
                       Gen.frequency((9, Gen.alphaNumChar), (1, Gen.const('-'))))
                     .map(_.mkString)
          nat <- Gen.oneOf(countriesValues)
        } yield MrzSearchFormModel(docType, docNum, yesterday, nat)

        forAll(validGen) { out =>
          val validInput =
            input(nationality = out.nationality, documentType = out.documentType, documentNumber = out.documentNumber)

          val bound = form.bind(validInput)
          bound.errors mustBe Nil
          bound.value mustBe Some(out)
        }
      }

      "year is 2 digit, default add 19XX" in {
        val yearStr = for {
          y1 <- Gen.numChar
          y2 <- Gen.numChar
        } yield s"$y1$y2"
        forAll(yearStr.suchThat(_.length == 2)) { year =>
          val validInput = inputYear(year = year)

          val out =
            MrzSearchFormModel("PASSPORT", "docNumber", yesterday.withYear(("19" + year).toInt), "AFG")
          val bound = form.bind(validInput)
          bound.errors mustBe Nil
          bound.value mustBe Some(out)
        }
      }

      "nationality and doc type are lower case" in {

        val validInput = input(nationality = "afg", documentType = "passport")
        val out = MrzSearchFormModel("PASSPORT", "docNumber", yesterday, "AFG")
        val bound = form.bind(validInput)

        bound.errors mustBe Nil
        bound.value mustBe Some(out)
      }

      "DocumentNumber contains spaces and spaces are removed" in {
        val validInput = input(documentNumber = "111 222 333")
        val out = MrzSearchFormModel("PASSPORT", "111222333", yesterday, "AFG")
        val bound = form.bind(validInput)

        bound.errors mustBe Nil
        bound.value mustBe Some(out)
      }
    }
  }

  "fail to bind" when {
    "dob is today" in {
      val invalidInput = input(dateOfBirth = now)

      form.bind(invalidInput).value must not be defined
      form.bind(invalidInput).errors mustBe List(FormError("dateOfBirth", List("error.dateOfBirth.invalid-format")))
    }

    "dob is future" in {
      val invalidInput = input(dateOfBirth = tomorrow)

      form.bind(invalidInput).value must not be defined
      form.bind(invalidInput).errors mustBe List(FormError("dateOfBirth", List("error.dateOfBirth.invalid-format")))
    }

    "nationality is empty" in {
      val invalidInput = input(nationality = "")

      form.bind(invalidInput).value must not be defined
      form.bind(invalidInput).errors mustBe List(FormError("nationality", List("error.nationality.required")))
    }

    "nationality contains invalid country code" in {
      val invalidNationality: Gen[String] = Gen.asciiPrintableStr
        .suchThat(_.trim.nonEmpty)
        .suchThat(sample => !countriesValues.contains(sample.toUpperCase))
        .suchThat(_.trim.nonEmpty)

      forAll(invalidNationality) { nationality =>
        val invalidInput = input(nationality = nationality)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("nationality", List("error.nationality.invalid")))
      }
    }

    "documentType is empty" in {
      val invalidInput = input(documentType = "")

      form.bind(invalidInput).value must not be defined
      form.bind(invalidInput).errors mustBe List(FormError("documentType", List("error.documentType.required")))
    }

    "documentType contains invalid chars" in {
      val invalidDocType: Gen[String] = Gen.asciiPrintableStr
        .suchThat(_.trim.nonEmpty)
        .suchThat(sample => !SearchByMRZForm.AllowedDocumentTypes.contains(sample.toUpperCase))
        .suchThat(_.trim.nonEmpty)

      forAll(invalidDocType) { documentType =>
        val invalidInput = input(documentType = documentType)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("documentType", List("error.documentType.invalid")))
      }
    }

    "documentNumber is empty" in {
      val invalidInput = input(documentNumber = "")

      form.bind(invalidInput).value must not be defined
      form.bind(invalidInput).errors mustBe List(FormError("documentNumber", List("error.documentNumber.required")))
    }

    "documentNumber is too long" in {
      val invalidDocNumber = Gen.asciiPrintableStr
        .suchThat(_.trim.nonEmpty)
        .suchThat(_.length > SearchByMRZForm.DocumentNumberMaxLength)

      forAll(invalidDocNumber) { documentNumber =>
        val invalidInput = input(documentNumber = documentNumber)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("documentNumber", List("error.documentNumber.invalid")))
      }
    }

    "documentNumber is invalid chars" in {
      val invalidDocNumber: Gen[String] =
        Gen
          .atLeastOne(Range(32, 44).map(_.toChar))
          .map(_.mkString)
          .suchThat(_.trim.nonEmpty)
          .suchThat(_.length <= SearchByMRZForm.DocumentNumberMaxLength)

      forAll(invalidDocNumber) { documentNumber =>
        val invalidInput = input(documentNumber = documentNumber)

        form.bind(invalidInput).value must not be defined
        form.bind(invalidInput).errors mustBe List(FormError("documentNumber", List("error.documentNumber.invalid")))
      }
    }
  }
}
