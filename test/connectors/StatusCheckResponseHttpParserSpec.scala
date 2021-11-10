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

class StatusCheckResponseHttpParserSpec extends AnyWordSpecLike with Matchers {

  "StatusCheckResponseReads.read" should {

    val TEAPOT = 418
    val errors = Seq[(Int, HomeOfficeError)](
      NOT_FOUND             -> StatusCheckNotFound,
      BAD_REQUEST           -> StatusCheckBadRequest,
      CONFLICT              -> StatusCheckConflict,
      INTERNAL_SERVER_ERROR -> StatusCheckInternalServerError,
      TEAPOT                -> OtherErrorResponse
    )

    "return a right where a 200 is returned with a valid response" in {
      val statusCheckResult = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
      val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
      val responseBody = Json.toJson(statusCheckResponse).toString
      val response = HttpResponse(OK, responseBody)

      val result: Either[HomeOfficeError, StatusCheckResponse] =
        StatusCheckResponseReads.read("POST", "some url", response)
      result.value shouldBe statusCheckResponse

    }

    "return a left where a 200 is returned without json" in {
      val responseBody = "This is not a valid response"
      val response = HttpResponse(OK, responseBody)

      val result: Either[HomeOfficeError, StatusCheckResponse] =
        StatusCheckResponseReads.read("POST", "some url", response)
      result.leftValue shouldBe StatusCheckInvalidResponse
    }

    "return a left where a 200 is returned with an invalid json response" in {
      val responseBody = """{"response": "Something"}"""
      val response = HttpResponse(OK, responseBody)

      val result: Either[HomeOfficeError, StatusCheckResponse] =
        StatusCheckResponseReads.read("POST", "some url", response)
      result.leftValue shouldBe StatusCheckInvalidResponse
    }

    errors.foreach { case (k, v) => checkError(k, v) }

  }

  def checkError(statusCode: Int, returnError: HomeOfficeError) =
    s"return a $returnError for status code $statusCode" in {
      val response = HttpResponse(statusCode, "responseBody")

      val result: Either[HomeOfficeError, StatusCheckResponse] =
        StatusCheckResponseReads.read("POST", "some url", response)
      result.leftValue shouldBe returnError
    }

}
