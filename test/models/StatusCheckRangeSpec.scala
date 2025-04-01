/*
 * Copyright 2025 HM Revenue & Customs
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

class StatusCheckRangeSpec extends AnyWordSpecLike with Matchers {

  "StatusCheckRange" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val range = StatusCheckRange(Some(LocalDate.of(2023, 12, 31)), Some(LocalDate.of(2023, 1, 1)))
        Json.toJson(range) shouldBe Json.obj(
          "endDate"   -> "2023-01-01",
          "startDate" -> "2023-12-31"
        )
      }

      "fields are empty" in {
        val range = StatusCheckRange(None, None)
        Json.toJson(range) shouldBe Json.obj()
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "endDate"   -> "2023-01-01",
          "startDate" -> "2023-12-31"
        )
        json.validate[StatusCheckRange] shouldBe JsSuccess(
          StatusCheckRange(Some(LocalDate.of(2023, 12, 31)), Some(LocalDate.of(2023, 1, 1)))
        )
      }

      "fields are empty" in {
        val json = Json.obj()
        json.validate[StatusCheckRange] shouldBe JsSuccess(StatusCheckRange(None, None))
      }

      "invalid date format" in {
        val json = Json.obj(
          "endDate"   -> "invalid-date",
          "startDate" -> "2023-12-31"
        )
        json.validate[StatusCheckRange] shouldBe a[JsError]
      }

      "invalid field types" in {
        val json = Json.obj(
          "endDate"   -> false,
          "startDate" -> false
        )
        json.validate[StatusCheckRange] shouldBe a[JsError]
      }
    }
  }
}
