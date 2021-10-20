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

package uk.gov.hmrc.homeofficeimmigrationstatus.forms

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.FormError
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoRequest
import org.scalatest.OptionValues
import org.scalacheck.Gen
import play.api.data.Form

class StatusCheckByNinoRequestFormSpec extends AnyWordSpecLike with Matchers with OptionValues {

  val form: Form[StatusCheckByNinoRequest] = {
    val provider = new StatusCheckByNinoFormProvider()
    provider()
  }

  val formOutput: StatusCheckByNinoRequest = StatusCheckByNinoRequest(
    nino = Nino("RJ301829A"),
    givenName = "Jan",
    familyName = "Kowalski",
    dateOfBirth = "1970-01-31")

  val formInput = Map(
    "dateOfBirth.year"  -> "1970",
    "dateOfBirth.month" -> "01",
    "dateOfBirth.day"   -> "31",
    "familyName"        -> "Kowalski",
    "givenName"         -> "Jan",
    "nino"              -> "RJ301829A")

  "StatusCheckByNinoRequestForm" should {

    //todo NINO GEN

    "bind some input fields and return StatusCheckByNinoRequest and fill it back" in {
      form.bind(formInput).value shouldBe Some(formOutput)
      form.fill(formOutput).data shouldBe formInput
    }

    "NINO tests" should {

      "report an error when NINO is missing" in {
        val input = formInput.updated("nino", "")
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.required"))
      }

      "report an error when NINO is invalid" in {
        val input = formInput.updated("nino", "invalid")
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "report an error when NINO is an string invalid by generated range" in {

        val generatedOption = Gen
          .oneOf(
            "AB888330E",
            "AB888330F",
            "AB888330G",
            "AB888330H",
            "AB888330I",
            "NT888330A",
            "DB888330A",
            "ZZ888330A",
            "BV888330A",
            "99888330A")
          .sample
          .value

        val input = formInput.updated("nino", generatedOption)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "report an error when NINO is to short" in {
        val input = formInput.updated("nino", "AA123456")
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "report an error when NINO is to long" in {
        val input = formInput.updated("nino", "AA123456AA")
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when characters D, F, I, Q, U and V are not used as either first or second letter" in {
        val generatedOptionFirstPlace = Gen.oneOf("D", "F", "I", "Q", "U", "V").sample.value
        val generatedOptionSecondPlace = Gen.oneOf("D", "F", "I", "Q", "U", "V").sample.value
        val baseNINO = "123456A"

        val input = formInput.updated("nino", generatedOptionFirstPlace + generatedOptionSecondPlace + baseNINO)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when Characters D, F, I, Q, U and V are not used as the first letter" in {
        val generatedOption = Gen.oneOf("D", "F", "I", "Q", "U", "V").sample.value
        val baseNINO = "A123456A"

        val input = formInput.updated("nino", generatedOption + baseNINO)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when characters D, F, I, Q, U, V and O are not used as the second letter" in {
        val generatedOption = Gen.oneOf("D", "F", "I", "Q", "U", "V", "O").sample.value
        val baseNINO = "123456A"

        val input = formInput.updated("nino", "A" + generatedOption + baseNINO)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when the prefixes BG, GB, KN, NK, NT, TN or ZN are used" in {
        val generatedOption = Gen.oneOf("BG", "GB", "NK", "KN", "TN", "TN", "ZZ").sample.value
        val baseNINO = "123456A"

        val input = formInput.updated("nino", "BG" + generatedOption + baseNINO)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when their are not six numbers" in {

        val prefix = "AA"
        val suffix = "A"

        val input = formInput.updated("nino", prefix + "12345" + suffix)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when there is a letter in the centre" in {

        val prefix = "AA"
        val suffix = "A"

        val input = formInput.updated("nino", prefix + "12A456" + suffix)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "not error when valid prefix passed" in {
        val input = formInput.updated("nino", "AA123456A")
        form.bind(input).errors shouldBe List()
      }

      "not error when Last character should be A, B, C or D" in {
        val generatedOption = Gen.oneOf("A", "B", "C", "D").sample.value
        val baseNINO = "AA123456"

        val input = formInput.updated("nino", baseNINO + generatedOption)
        form.bind(input).errors shouldBe List()
      }

      "error when last character is not be A, B, C or D" in {
        val generatedOption = Gen.oneOf("E", "F", "G", "H").sample.value
        val baseNINO = "AA123456"

        val input = formInput.updated("nino", baseNINO + generatedOption)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }

      "error when special characters are used" in {
        val generatedOption = Gen.oneOf("!", "£", "#", "%").sample.value
        val baseNINO = "AA123456"

        val input = formInput.updated("nino", baseNINO + generatedOption)
        form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
      }
    }

    "First Name tests" should {

      "report an error when givenName is missing" in {
        val input = formInput.updated("givenName", "")
        form.bind(input).errors shouldBe List(FormError("givenName", "error.givenName.required"))
      }

      "report an error when givenName only numbers invalid" in {
        val input = formInput.updated("givenName", "11267162")
        form.bind(input).errors shouldBe List(FormError("givenName", "error.givenName.invalid-format"))
      }

      "report an error when special characters are used" in {
        val generatedOption = Gen.oneOf("!", "£", "#", "%").sample.value
        val input = formInput.updated("givenName", generatedOption)
        form.bind(input).errors shouldBe List(FormError("givenName", "error.givenName.invalid-format"))
      }

      "not error reported when givenName is atleast one character long" in {
        val input = formInput.updated("givenName", "a")
        form.bind(input).errors shouldBe List()
      }

      "not report an error when givenName contains a -" in {
        val input = formInput.updated("givenName", "-")
        form.bind(input).errors shouldBe List()
      }

      "not report an error when givenName contains is a -" in {
        val input = formInput.updated("givenName", "a-b")
        form.bind(input).errors shouldBe List()
      }
    }

    "Family Name Tests" should {

      "report an error when familyName is missing" in {
        val input = formInput.updated("familyName", "")
        form.bind(input).errors shouldBe List(FormError("familyName", "error.familyName.required"))
      }

      "report an error when familyName is too short" in {
        val input = formInput.updated("familyName", "A")
        form.bind(input).errors shouldBe List(FormError("familyName", "error.familyName.invalid-format"))
      }

      "no error reported when familyName is two or more characters" in {
        val input = formInput.updated("familyName", "Aa")
        form.bind(input).errors shouldBe List()
      }

      "report an error when familyName only numbers are used" in {
        val input = formInput.updated("familyName", "11267162")
        form.bind(input).errors shouldBe List(FormError("familyName", "error.familyName.invalid-format"))
      }

      "report an error when special characters are used" in {
        val generatedOption = Gen.oneOf("!", "£", "#", "%").sample.value
        val input = formInput.updated("familyName", generatedOption)
        form.bind(input).errors shouldBe List(FormError("familyName", "error.familyName.invalid-format"))
      }

      "not report an error when givenName contains is a -" in {
        val input = formInput.updated("familyName", "a-b")
        form.bind(input).errors shouldBe List()
      }
    }

    "Date of Birth (DOB)" should {

      "report an errpr when dateOfBirth.day is missing" in {
        val input = formInput.updated("dateOfBirth.day", "")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.month is missing" in {
        val input = formInput.updated("dateOfBirth.month", "")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is missing" in {
        val input = formInput.updated("dateOfBirth.year", "")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.day is invalid" in {

        val generatedOption = Gen
          .oneOf("32", "XH", "-5", "0H")
          .sample
          .value

        val input = formInput.updated("dateOfBirth.day", generatedOption)
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.day is invalid - contains digit and wildcard" in {
        val input = formInput.updated("dateOfBirth.day", "0X")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.day is invalid - contains value out-of-scope" in {
        val input = formInput.updated("dateOfBirth.day", "32")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.day is invalid - contains negative value" in {
        val input = formInput.updated("dateOfBirth.day", "-5")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.month is invalid" in {
        val generatedOption = Gen
          .oneOf("13", "X8", "XX", "-5")
          .sample
          .value

        val input = formInput.updated("dateOfBirth.month", generatedOption)
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.month is invalid - contains digit and wildcard" in {
        val input = formInput.updated("dateOfBirth.month", "1X")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.month is invalid - contains value out-of-scope" in {
        val input = formInput.updated("dateOfBirth.month", "13")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.month is invalid - contains negative value" in {
        val input = formInput.updated("dateOfBirth.month", "-5")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is invalid" in {
        val generatedOption = Gen
          .oneOf("193", "197B", "196X", "-5", "11111", "999123")
          .sample
          .value

        val input = formInput.updated("dateOfBirth.year", generatedOption)
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is invalid - too short" in {
        val input = formInput.updated("dateOfBirth.year", "193")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is invalid - contains letter" in {
        val input = formInput.updated("dateOfBirth.year", "197B")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is invalid - contains value out-of-scope" in {
        val input = formInput.updated("dateOfBirth.year", "999123")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }

      "report an error when dateOfBirth.year is invalid - contains negative value" in {
        val input = formInput.updated("dateOfBirth.month", "-5")
        form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
      }
    }
  }
}
