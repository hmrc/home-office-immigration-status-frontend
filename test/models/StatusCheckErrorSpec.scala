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

class StatusCheckErrorSpec extends AnyWordSpecLike with Matchers {
  "StatusCheckError" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val error = StatusCheckError("404", Some(List(FieldError("error1", "field1"))))
        Json.toJson(error) shouldBe Json.obj(
          "errCode" -> "404",
          "fields" -> Json.arr(
            Json.obj(
              "code" -> "error1",
              "name" -> "field1"
            )
          )
        )
      }

      "fields are empty" in {
        val error = StatusCheckError("404", None)
        Json.toJson(error) shouldBe Json.obj(
          "errCode" -> "404"
        )
      }

      "fields are empty2" in {
        val error = StatusCheckError("404")
        Json.toJson(error) shouldBe Json.obj(
          "errCode" -> "404"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "errCode" -> "404",
          "fields" -> Json.arr(
            Json.obj(
              "name" -> "field1",
              "code" -> "error1"
            )
          )
        )
        json.validate[StatusCheckError] shouldBe JsSuccess(
          StatusCheckError("404", Some(List(FieldError("error1", "field1"))))
        )
      }

      "fields are empty" in {
        val json = Json.obj(
          "errCode" -> ""
        )
        json.validate[StatusCheckError] shouldBe JsSuccess(StatusCheckError("", None))
      }

      "invalid field types" in {
        val json = Json.obj(
          "errCode" -> 404,
          "fields" -> Json.arr(
            Json.obj(
              "name" -> 123,
              "code" -> true
            )
          )
        )
        json.validate[StatusCheckError] shouldBe a[JsError]
      }
    }
  }
}
