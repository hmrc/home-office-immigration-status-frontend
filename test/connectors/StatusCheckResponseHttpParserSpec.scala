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

package connectors

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HttpResponse
import models._
import java.time.LocalDate
import play.api.libs.json.Json
import connectors.StatusCheckResponseHttpParser._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cats.scalatest.EitherValues._
import play.api.http.Status._
import models.StatusCheckError._

class StatusCheckResponseHttpParserSpec extends AnyWordSpecLike with Matchers {

  "StatusCheckResponseReads.read" should {

    implicit val resultWrites = Json.writes[StatusCheckResult]
    val responseWrites = Json.writes[StatusCheckSuccessfulResponse]

    "return a success where a 200 is returned with a valid response" in {
      val statusCheckResult = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
      val statusCheckResponse = StatusCheckSuccessfulResponse(Some("CorrelationId"), statusCheckResult)
      val responseBody = Json.toJson(statusCheckResponse)(responseWrites).toString
      val response = HttpResponse(OK, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

      val expectedResponse = StatusCheckResponseWithStatus(OK, statusCheckResponse)

      val result = StatusCheckResponseReads.read("POST", "some url", response)
      result shouldBe expectedResponse

    }

    "return an unknown error" when {

      val unknownErrorResponse = StatusCheckResponseWithStatus(
        INTERNAL_SERVER_ERROR,
        StatusCheckErrorResponse(Some("correlationId"), StatusCheckError("UNKNOWN_ERROR")))

      "a 200 is returned without json" in {
        val responseBody = "This is not a valid response"
        val response = HttpResponse(OK, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe unknownErrorResponse
      }

      "a 200 is returned with an invalid json response" in {
        val responseBody = """{"response": "Something"}"""
        val response = HttpResponse(OK, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe unknownErrorResponse
      }

      "a non-200 is returned without json" in {
        val responseBody = "This is not a valid response"
        val response = HttpResponse(OK, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe unknownErrorResponse
      }

      "a non-200 is returned with an invalid json response" in {
        val responseBody = """{"response": "Something"}"""
        val response =
          HttpResponse(INTERNAL_SERVER_ERROR, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe unknownErrorResponse
      }
    }

    "return the home office error" when {
      "a 500 status code is returned with a valid error response" in {
        val statusCheckError = StatusCheckError("Oh no!")
        val statusCheckResponse = StatusCheckErrorResponse(Some("CorrelationId"), statusCheckError)
        val responseBody = Json.toJson(statusCheckResponse).toString
        val response =
          HttpResponse(INTERNAL_SERVER_ERROR, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val expectedResponse = StatusCheckResponseWithStatus(INTERNAL_SERVER_ERROR, statusCheckResponse)

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe expectedResponse
      }

      "a 404 status code is returned with a valid error response" in {
        val statusCheckError = StatusCheckError("Oh no!")
        val statusCheckResponse = StatusCheckErrorResponse(Some("CorrelationId"), statusCheckError)
        val responseBody = Json.toJson(statusCheckResponse).toString
        val response = HttpResponse(NOT_FOUND, responseBody, Map("X-Correlation-Id" -> Seq("correlationId")))

        val expectedResponse = StatusCheckResponseWithStatus(NOT_FOUND, statusCheckResponse)

        val result = StatusCheckResponseReads.read("POST", "some url", response)
        result shouldBe expectedResponse
      }
    }

  }

}
