/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.{LocalDate, ZoneId}
import java.util.UUID

import models._
import org.mockito.Mockito.mock
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.SessionCacheRepository
import stubs.HomeOfficeImmigrationStatusStubs
import support.{BaseISpec, WireMockSupport}
import uk.gov.hmrc.http._

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeImmigrationStatusConnectorISpec extends HomeOfficeImmigrationStatusConnectorISpecSetup {

  "HomeOfficeImmigrationStatusProxyConnector" when {

    "statusPublicFundsByNino" should {

      "return status when successful" in {
        givenCheckByNinoSucceeds()

        val result = connector.statusPublicFundsByNino(request).futureValue

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

        val result = connector.statusPublicFundsByNino(request).futureValue

        val expectedResult = StatusCheckResponseWithStatus(
          BAD_REQUEST,
          StatusCheckErrorResponse(Some(correlationId), StatusCheckError("ERR_REQUEST_INVALID"))
        )

        result shouldBe expectedResult
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result = connector.statusPublicFundsByNino(request).futureValue

        val expectedResult = StatusCheckResponseWithStatus(
          NOT_FOUND,
          StatusCheckErrorResponse(Some(correlationId), StatusCheckError("ERR_NOT_FOUND"))
        )

        result shouldBe expectedResult
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result = connector.statusPublicFundsByNino(request).futureValue

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

        val result = connector.statusPublicFundsByNino(request).futureValue

        val expectedResult = StatusCheckResponseWithStatus(
          CONFLICT,
          StatusCheckErrorResponse(Some("some-correlation-id"), StatusCheckError("UNKNOWN_ERROR"))
        )

        result shouldBe expectedResult
      }

      "throw exception if 5xx response" in {
        givenAnExternalServiceErrorCheckByNino

        val result = connector.statusPublicFundsByNino(request).futureValue

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
  implicit val hc: HeaderCarrier =
    HeaderCarrier().withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString)

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit lazy val fakeApplication: Application = appBuilder.build()

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
