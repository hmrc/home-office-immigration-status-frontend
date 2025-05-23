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

class StatusCheckResponseSpec extends AnyWordSpecLike with Matchers {

  private val error = StatusCheckError("ERR_CODE", Some(List(FieldError("error1", "field1"))))

  def makeImmigrationStatus(daysAgo: Int = 0): ImmigrationStatus =
    ImmigrationStatus(
      LocalDate.now.minusDays(daysAgo),
      None,
      "some product type",
      "some immigration status",
      noRecourseToPublicFunds = true
    )

  "StatusCheckErrorResponse" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val response = StatusCheckErrorResponse(
          Some("correlationId123"),
          error
        )
        Json.toJson(response) shouldBe Json.obj(
          "correlationId" -> "correlationId123",
          "error"         -> Json.toJson(error)
        )
      }

      "correlationId is empty" in {
        val response = StatusCheckErrorResponse(
          None,
          error
        )
        Json.toJson(response) shouldBe Json.obj(
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
        json.validate[StatusCheckErrorResponse] shouldBe JsSuccess(
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
        json.validate[StatusCheckErrorResponse] shouldBe JsSuccess(
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
        json.validate[StatusCheckErrorResponse] shouldBe a[JsError]
      }
    }
  }

  "StatusCheckSuccessfulResponse.writes" should {
    "Convert to json without the type" in {
      val expected = makeImmigrationStatus()
      val date     = LocalDate.now
      val result   = StatusCheckResult("some name", date, "some nationality", List(expected))
      val search: StatusCheckSuccessfulResponse = StatusCheckSuccessfulResponse(
        Some("correlationId"),
        result
      )

      Json.toJson(search) shouldEqual Json.parse(
        s"""{"result":{"fullName":"some name","dateOfBirth":"$date","nationality":"some nationality","mostRecentStatus":
           |{"statusStartDate":"$date","productType":"some product type","immigrationStatus":"some immigration status","noRecourseToPublicFunds":true},"previousStatuses":[]},"correlationId":"correlationId"}""".stripMargin
      )
    }
  }

}
