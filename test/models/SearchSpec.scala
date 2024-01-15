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

package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import utils.NinoGenerator

import java.time.LocalDate

class SearchSpec extends PlaySpec {

  val date: LocalDate = LocalDate.now

  "writes" should {
    "Convert to json without the type" when {
      "it's an MrzSearch" in {
        val search: Search = MrzSearch(
          "documentType",
          "documentNumber",
          date,
          "nationality",
          StatusCheckRange(Some(date), Some(date))
        )
        Json.toJson(search) mustEqual Json.parse(
          s"""{"documentType":"documentType","documentNumber":"documentNumber","dateOfBirth":"${date.toString}",
             |"nationality":"nationality","statusCheckRange":{"startDate":"${date.toString}","endDate":"${date.toString}"}}""".stripMargin
        )
      }

      "it's a NinoSearch" in {
        val nino = NinoGenerator.generateNino
        val search: Search = NinoSearch(
          nino,
          "given",
          "family",
          date.toString,
          StatusCheckRange(Some(date), Some(date))
        )
        Json.toJson(search) mustEqual Json.parse(
          s"""{"nino":"${nino.toString}","givenName":"given","familyName":"family","dateOfBirth":"${date.toString}",
             |"statusCheckRange":{"startDate":"${date.toString}","endDate":"${date.toString}"}}""".stripMargin
        )
      }
    }
  }

}
