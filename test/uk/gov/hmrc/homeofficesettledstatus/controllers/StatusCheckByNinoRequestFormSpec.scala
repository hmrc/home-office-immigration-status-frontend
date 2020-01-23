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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.models.StatusCheckByNinoRequest
import uk.gov.hmrc.play.test.UnitSpec

class StatusCheckByNinoRequestFormSpec extends UnitSpec {

  val formOutput = StatusCheckByNinoRequest(
    dateOfBirth = "1970-01-31",
    familyName = "KOWALSKI",
    givenName = "JAN",
    nino = Nino("RJ301829A")
  )

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
  }
}
