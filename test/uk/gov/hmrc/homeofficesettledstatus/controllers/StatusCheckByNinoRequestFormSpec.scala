/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.data.FormError
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.models.StatusCheckByNinoRequest
import uk.gov.hmrc.play.test.UnitSpec

class StatusCheckByNinoRequestFormSpec extends UnitSpec {

  val formOutput = StatusCheckByNinoRequest(
    nino = Nino("RJ301829A"),
    givenName = "JAN",
    familyName = "KOWALSKI",
    dateOfBirth = "1970-01-31")

  val formInput1 = Map(
    "dateOfBirth.year"  -> "1970",
    "dateOfBirth.month" -> "01",
    "dateOfBirth.day"   -> "31",
    "familyName"        -> "Kowalski",
    "givenName"         -> "Jan",
    "nino"              -> "RJ301829A")

  val formInput2 = Map(
    "dateOfBirth.year"  -> "1970",
    "dateOfBirth.month" -> "01",
    "dateOfBirth.day"   -> "31",
    "familyName"        -> "KOWALSKI",
    "givenName"         -> "JAN",
    "nino"              -> "RJ301829A")

  "StatusCheckByNinoRequestForm" should {

    "bind some input fields and return StatusCheckByNinoRequest and fill it back" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm

      form.bind(formInput1).value shouldBe Some(formOutput)
      form.fill(formOutput).data shouldBe formInput2
    }

    "report error when NINO is missing" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("nino", "")
      form.bind(input).errors shouldBe List(FormError("nino", "error.nino.required"))
    }

    "report error when NINO is invalid" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("nino", "invalid")
      form.bind(input).errors shouldBe List(FormError("nino", "error.nino.invalid-format"))
    }

    "report error when givenName is missing" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("givenName", "")
      form.bind(input).errors shouldBe List(FormError("givenName", "error.givenName.required"))
    }

    "report error when givenName is invalid" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("givenName", "11267162")
      form.bind(input).errors shouldBe List(
        FormError("givenName", "error.givenName.invalid-format"))
    }

    "report error when familyName is missing" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("familyName", "")
      form.bind(input).errors shouldBe List(FormError("familyName", "error.familyName.required"))
    }

    "report error when familyName is invalid" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("familyName", "AB")
      form.bind(input).errors shouldBe List(
        FormError("familyName", "error.familyName.invalid-format"))
    }

    "report error when dateOfBirth.year is missing" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.year", "")
      form.bind(input).errors shouldBe List(FormError("dateOfBirth", "error.dateOfBirth.required"))
    }

    "report error when dateOfBirth.year is invalid" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.year", "197B")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "report error when dateOfBirth.day is invalid - contains digit and wildcard" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.day", "0X")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "report error when dateOfBirth.day is invalid - contains value out-of-scope" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.day", "32")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "report error when dateOfBirth.month is invalid - contains digit and wildcard" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.month", "1X")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "report error when dateOfBirth.month is invalid - contains value out-of-scope" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.month", "13")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "allow empty dateOfBirth.day" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.day", "")
      form.bind(input).value shouldBe Some(formOutput.copy(dateOfBirth = "1970-01-XX"))
    }

    "report error when empty dateOfBirth.month " in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.month", "")
      form.bind(input).errors shouldBe List(
        FormError("dateOfBirth", "error.dateOfBirth.invalid-format"))
    }

    "allow empty dateOfBirth.day and empty dateOfBirth.month" in {
      val form = HomeOfficeSettledStatusFrontendController.StatusCheckByNinoRequestForm
      val input = formInput1.updated("dateOfBirth.day", "").updated("dateOfBirth.month", "")
      form.bind(input).value shouldBe Some(formOutput.copy(dateOfBirth = "1970-XX-XX"))
    }
  }
}
