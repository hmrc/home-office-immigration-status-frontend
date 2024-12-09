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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDate

class ImmigrationStatusSpec extends AnyWordSpecLike with Matchers {

  val startDate: LocalDate = LocalDate.parse("2012-01-01")
  val endDate: LocalDate   = LocalDate.parse("2013-01-01")

  "ImmigrationStatus" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val status = ImmigrationStatus(startDate, Some(endDate), "product", "status", true)
        Json.toJson(status) shouldBe Json.obj(
          "productType"             -> "product",
          "statusEndDate"           -> "2013-01-01",
          "statusStartDate"         -> "2012-01-01",
          "noRecourseToPublicFunds" -> true,
          "immigrationStatus"       -> "status"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "statusStartDate"         -> "2012-01-01",
          "statusEndDate"           -> "2013-01-01",
          "productType"             -> "product",
          "immigrationStatus"       -> "status",
          "noRecourseToPublicFunds" -> true
        )
        json.validate[ImmigrationStatus] shouldBe JsSuccess(
          ImmigrationStatus(startDate, Some(endDate), "product", "status", true)
        )
      }

      "fields are empty" in {
        val json = Json.obj()
        json.validate[ImmigrationStatus] shouldBe a[JsError]
      }

      "invalid field types" in {
        val json = Json.obj(
          "statusStartDate"         -> 0,
          "statusEndDate"           -> 0,
          "productType"             -> 0,
          "immigrationStatus"       -> 0,
          "noRecourseToPublicFunds" -> 0
        )
        json.validate[ImmigrationStatus] shouldBe a[JsError]
      }
    }
  }
}
