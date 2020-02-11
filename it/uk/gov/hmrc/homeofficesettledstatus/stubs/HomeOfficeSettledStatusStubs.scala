package uk.gov.hmrc.homeofficesettledstatus.stubs

import java.time.{LocalDate, ZoneId}

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.homeofficesettledstatus.support.WireMockSupport

trait HomeOfficeSettledStatusStubs {
  me: WireMockSupport =>

  def validRequestBodyWith3MonthsDateRange = {
    val date = LocalDate.now(ZoneId.of("UTC"))
    requestBodyWithRange(date.minusMonths(3).toString, date.toString)
  }

  def requestBodyWithRange(startDate: String, endDate: String): String =
    s"""{
       |  "dateOfBirth": "2001-01-31",
       |  "familyName": "JANE",
       |  "givenName": "DOE",
       |  "nino": "RJ301829A",
       |  "statusCheckRange": {
       |    "startDate": "$startDate",
       |    "endDate": "$endDate"
       |  }
       |}""".stripMargin

  val invalidNinoRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "JANE",
      |  "givenName": "DOE",
      |  "nino": "invailid"
      |}""".stripMargin

  val responseBodyWithStatus: String =
    """{
      |  "correlationId": "sjdfhks123",
      |  "result": {
      |    "dateOfBirth": "2001-01-31",
      |    "facialImage": "string",
      |    "fullName": "Jane Doe",
      |    "statuses": [
      |      {
      |        "immigrationStatus": "ILR",
      |        "rightToPublicFunds": true,
      |        "statusEndDate": "2018-01-31",
      |        "statusStartDate": "2018-12-12"
      |      }
      |    ]
      |  }
      |}""".stripMargin

  def givenStatusCheckSucceeds(): StubMapping =
    givenStatusPublicFundsByNinoStub(
      200,
      validRequestBodyWith3MonthsDateRange,
      responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoStub(
      200,
      validRequestBodyWith3MonthsDateRange,
      responseBodyWithStatus)

  def givenStatusCheckErrorWhenMissingInputField(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(400, validRequestBodyWith3MonthsDateRange, errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(404, validRequestBodyWith3MonthsDateRange, errorResponseBody)
  }

  def givenStatusCheckErrorWhenConflict(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_CONFLICT"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(409, validRequestBodyWith3MonthsDateRange, errorResponseBody)
  }

  def givenStatusCheckErrorWhenDOBInvalid(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
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

    givenStatusPublicFundsByNinoStub(422, validRequestBodyWith3MonthsDateRange, errorResponseBody)

  }

  def givenStatusPublicFundsByNinoStub(
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/nino"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

}
