/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.{JsError, JsSuccess, Json}
import support.BaseSpec

import java.time.LocalDate

class StatusCheckResponseSpec extends BaseSpec {

  private val error = StatusCheckError("ERR_CODE", Some(List(FieldError("error1", "field1"))))

  def makeImmigrationStatus(daysAgo: Int = 0): ImmigrationStatus =
    ImmigrationStatus(
      LocalDate.now.minusDays(daysAgo),
      None,
      "some product type",
      "some immigration status",
      noRecourseToPublicFunds = true
    )

  "StatusCheckErrorResponse" must {
    "serialize to JSON" when {
      "all fields are defined" in {
        val response = StatusCheckErrorResponse(
          Some("correlationId123"),
          error
        )
        Json.toJson(response) mustBe Json.obj(
          "correlationId" -> "correlationId123",
          "error"         -> Json.toJson(error)
        )
      }

      "correlationId is empty" in {
        val response = StatusCheckErrorResponse(
          None,
          error
        )
        Json.toJson(response) mustBe Json.obj(
          "error" -> Json.toJson(error)
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "correlationId" -> "correlationId123",
          "error" -> Json.obj(
            "errCode" -> "ERR_CODE",
            "fields" -> Json.arr(
              Json.obj(
                "code" -> "error1",
                "name" -> "field1"
              )
            )
          )
        )
        json.validate[StatusCheckErrorResponse] mustBe JsSuccess(
          StatusCheckErrorResponse(
            Some("correlationId123"),
            error
          )
        )
      }

      "correlationId is empty" in {
        val json = Json.obj(
          "error" -> Json.obj(
            "errCode" -> "ERR_CODE",
            "fields" -> Json.arr(
              Json.obj(
                "code" -> "error1",
                "name" -> "field1"
              )
            )
          )
        )
        json.validate[StatusCheckErrorResponse] mustBe JsSuccess(
          StatusCheckErrorResponse(
            None,
            error
          )
        )
      }

      "invalid field types" in {
        val json = Json.obj(
          "correlationId" -> 12345,
          "error"         -> "Invalid"
        )
        json.validate[StatusCheckErrorResponse] mustBe a[JsError]
      }
    }
  }

  "StatusCheckSuccessfulResponse.writes" must {
    "Convert to json without the type" in {
      val expected = makeImmigrationStatus()
      val date     = LocalDate.now
      val result   = StatusCheckResult("some name", date, "some nationality", List(expected))
      val search: StatusCheckSuccessfulResponse = StatusCheckSuccessfulResponse(
        Some("correlationId"),
        result
      )

      Json.toJson(search) mustEqual Json.parse(
        s"""{"result":{"fullName":"some name","dateOfBirth":"$date","nationality":"some nationality","mostRecentStatus":
           |{"statusStartDate":"$date","productType":"some product type","immigrationStatus":"some immigration status","noRecourseToPublicFunds":true},"previousStatuses":[]},"correlationId":"correlationId"}""".stripMargin
      )
    }
  }

}
