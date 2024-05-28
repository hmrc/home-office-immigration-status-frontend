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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.mvc.Http.HeaderNames
import support.WireMockSupport
import utils.NinoGenerator
import uk.gov.hmrc.domain.Nino

import java.time.{LocalDate, ZoneId}

trait HomeOfficeImmigrationStatusStubs extends JourneyTestData {
  me: WireMockSupport =>

  val queryMonths: Int = 6

  def validByNinoRequestBody(): String = {
    val date = LocalDate.now(ZoneId.of("UTC"))
    byNinoBodyWithRange(date.minusMonths(queryMonths).toString, date.toString)
  }

  def validByMrzRequestBody: String = {
    val date = LocalDate.now(ZoneId.of("UTC"))
    byMrzBodyWithRange(date.minusMonths(queryMonths).toString, date.toString)
  }

  val nino: Nino = NinoGenerator.generateNino

  def byMrzBodyWithRange(startDate: String, endDate: String): String =
    s"""{
       |  "dateOfBirth": "2001-01-31",
       |  "nationality": "AFG",
       |  "documentNumber": "123456789",
       |  "documentType": "PASSPORT",
       |  "statusCheckRange": {
       |    "startDate": "$startDate",
       |    "endDate": "$endDate"
       |  }
       |}""".stripMargin

  def byNinoBodyWithRange(startDate: String, endDate: String): String =
    s"""{
       |  "dateOfBirth": "2001-01-31",
       |  "familyName": "Jane",
       |  "givenName": "Doe",
       |  "nino": "${nino.nino}",
       |  "statusCheckRange": {
       |    "startDate": "$startDate",
       |    "endDate": "$endDate"
       |  }
       |}""".stripMargin

  val invalidNinoRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "invalid"
      |}""".stripMargin

  val responseBodyWithStatus: String =
    s"""{
       |  "correlationId": "$correlationId",
       |  "result": {
       |    "dateOfBirth": "2001-01-31",
       |    "nationality": "IRL",
       |    "fullName": "Jane Doe",
       |    "statuses": [
       |      {
       |        "productType": "EUS",
       |        "immigrationStatus": "ILR",
       |        "noRecourseToPublicFunds": true,
       |        "statusEndDate": "2018-01-31",
       |        "statusStartDate": "2018-12-12"
       |      }
       |    ]
       |  }
       |}""".stripMargin

  def givenCheckByNinoSucceeds(): StubMapping =
    givenStatusPublicFundsCheckStub("nino", OK, validByNinoRequestBody(), responseBodyWithStatus)

  def givenCheckByMrzSucceeds(): StubMapping =
    givenStatusPublicFundsCheckStub("mrz", OK, validByMrzRequestBody, responseBodyWithStatus)

  def givenCheckByNinoErrorWhenMissingInputField(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_REQUEST_INVALID"
         |  }
         |}""".stripMargin

    givenStatusPublicFundsCheckStub("nino", BAD_REQUEST, validByNinoRequestBody(), errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_NOT_FOUND"
         |  }
         |}""".stripMargin

    givenStatusPublicFundsCheckStub("nino", NOT_FOUND, validByNinoRequestBody(), errorResponseBody)
  }

  def givenAnExternalServiceErrorCheckByNino(): StubMapping =
    givenStatusPublicFundsCheckStub("nino", INTERNAL_SERVER_ERROR, validByNinoRequestBody(), "", "some-correlation-id")

  def givenStatusCheckErrorWhenDOBInvalid(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_VALIDATION",
         |    "fields": [
         |      {
         |        "code": "ERR_INVALID_DOB",
         |        "name": "dateOfBirth"
         |      }
         |    ]
         |  }
         |}""".stripMargin

    givenStatusPublicFundsCheckStub("nino", BAD_REQUEST, validByNinoRequestBody(), errorResponseBody)

  }

  def givenStatusPublicFundsCheckStub(
    endpoint: String,
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String,
    correlationId: String = "correlationId"
  ): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/$endpoint"))
        .withHeader("X-Correlation-Id", new AnythingPattern())
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Correlation-Id", correlationId)
            .withBody(responseBody)
        )
    )
}
