package stubs

import java.time.{LocalDate, ZoneId}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.mvc.Http.HeaderNames
import support.WireMockSupport

trait HomeOfficeImmigrationStatusStubs extends JourneyTestData {
  me: WireMockSupport =>

  val queryMonths: Int = 6

  def validRequestBodyWithDateRange(): String = {
    val date = LocalDate.now(ZoneId.of("UTC"))
    requestBodyWithRange(date.minusMonths(queryMonths).toString, date.toString)
  }

  def requestBodyWithRange(startDate: String, endDate: String): String =
    s"""{
       |  "dateOfBirth": "2001-01-31",
       |  "familyName": "Jane",
       |  "givenName": "Doe",
       |  "nino": "RJ301829A",
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

  def givenStatusCheckSucceeds(): StubMapping =
    givenStatusPublicFundsByNinoStub(200, validRequestBodyWithDateRange(), responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoStub(200, validRequestBodyWithDateRange(), responseBodyWithStatus)

  def givenStatusCheckErrorWhenMissingInputField(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_REQUEST_INVALID"
         |  }
         |}""".stripMargin

    givenStatusPublicFundsByNinoStub(400, validRequestBodyWithDateRange(), errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_NOT_FOUND"
         |  }
         |}""".stripMargin

    givenStatusPublicFundsByNinoStub(404, validRequestBodyWithDateRange(), errorResponseBody)
  }

  def givenStatusCheckErrorWhenConflict(): StubMapping = {

    val errorResponseBody: String =
      s"""{
         |  "correlationId": "$correlationId",
         |  "error": {
         |    "errCode": "ERR_CONFLICT"
         |  }
         |}""".stripMargin

    givenStatusPublicFundsByNinoStub(409, validRequestBodyWithDateRange(), errorResponseBody)
  }

  def givenAnExternalServiceError(): StubMapping =
    givenStatusPublicFundsByNinoErrorStub(500, validRequestBodyWithDateRange())

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

    givenStatusPublicFundsByNinoStub(400, validRequestBodyWithDateRange(), errorResponseBody)

  }

  def givenStatusPublicFundsByNinoStub(httpResponseCode: Int, requestBody: String, responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/nino"))
        .withHeader("X-Correlation-Id", new AnythingPattern())
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

  def givenStatusPublicFundsByNinoErrorStub(httpResponseCode: Int, requestBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/nino"))
        .withHeader("X-Correlation-Id", new AnythingPattern())
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
        ))
}
