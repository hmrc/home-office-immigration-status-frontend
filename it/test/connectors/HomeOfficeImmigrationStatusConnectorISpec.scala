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

package connectors

import models._
import org.mockito.Mockito.mock
import play.api.Application
import play.api.http.Status._
import repositories.SessionCacheRepository
import stubs.HomeOfficeImmigrationStatusStubs
import support.BaseISpec
import uk.gov.hmrc.http._
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import java.time.{LocalDate, ZoneId}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeImmigrationStatusConnectorISpec extends HomeOfficeImmigrationStatusConnectorISpecSetup {

  "HomeOfficeImmigrationStatusProxyConnector" when {

    "statusPublicFundsByNino" should {

      "return status when successful" in {
        givenCheckByNinoSucceeds()

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResult(
          fullName = "Jane Doe",
          dateOfBirth = LocalDate.parse("2001-01-31"),
          nationality = "IRL",
          statuses = List(
            ImmigrationStatus(
              productType = "EUS",
              immigrationStatus = "ILR",
              noRecourseToPublicFunds = true,
              statusEndDate = Some(LocalDate.parse("2018-01-31")),
              statusStartDate = LocalDate.parse("2018-12-12")
            )
          )
        )
        val expectedResponse =
          StatusCheckResponseWithStatus(OK, StatusCheckSuccessfulResponse(Some(correlationId), expectedResult))

        result shouldBe expectedResponse
      }

      "return check error when 400 response ERR_REQUEST_INVALID" in {
        givenCheckByNinoErrorWhenMissingInputField()

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResponseWithStatus(
          BAD_REQUEST,
          StatusCheckErrorResponse(Some(correlationId), StatusCheckError("ERR_REQUEST_INVALID"))
        )

        result shouldBe expectedResult
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResponseWithStatus(
          NOT_FOUND,
          StatusCheckErrorResponse(Some(correlationId), StatusCheckError("ERR_NOT_FOUND"))
        )

        result shouldBe expectedResult
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResponseWithStatus(
          BAD_REQUEST,
          StatusCheckErrorResponse(
            Some(correlationId),
            StatusCheckError("ERR_VALIDATION", Option(Seq(FieldError("ERR_INVALID_DOB", "dateOfBirth"))))
          )
        )

        result shouldBe expectedResult
      }

      "return check error if invalid JSON body return" in {
        givenStatusPublicFundsCheckStub("nino", CONFLICT, validByNinoRequestBody(), "", "some-correlation-id")

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResponseWithStatus(
          CONFLICT,
          StatusCheckErrorResponse(Some("some-correlation-id"), StatusCheckError("UNKNOWN_ERROR"))
        )

        result shouldBe expectedResult
      }

      "throw exception if 5xx response" in {
        givenAnExternalServiceErrorCheckByNino()

        val result = await(connector.statusPublicFundsByNino(request))

        val expectedResult = StatusCheckResponseWithStatus(
          INTERNAL_SERVER_ERROR,
          StatusCheckErrorResponse(Some("some-correlation-id"), StatusCheckError("UNKNOWN_ERROR"))
        )

        result shouldBe expectedResult
      }
    }
  }

}

trait HomeOfficeImmigrationStatusConnectorISpecSetup extends BaseISpec with HomeOfficeImmigrationStatusStubs {

  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"
  given hc: HeaderCarrier =
    HeaderCarrier().withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString)

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit def fakeApplication(): Application = appBuilder.build()

  lazy val connector: HomeOfficeImmigrationStatusProxyConnector =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyConnector]

  val request: NinoSearch = NinoSearch(
    nino,
    "Doe",
    "Jane",
    "2001-01-31",
    StatusCheckRange(
      Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(queryMonths)),
      Some(LocalDate.now(ZoneId.of("UTC")))
    )
  )
}
