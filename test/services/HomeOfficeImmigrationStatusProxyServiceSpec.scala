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

package services

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
import models.HomeOfficeError._
import controllers.ControllerSpec
import org.mockito.Mockito.{mock, never, reset, verify, when}
import org.mockito.ArgumentMatchers.{any, refEq, eq => is}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.mvc.Request
import services.HomeOfficeImmigrationStatusFrontendEvent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.format.DateTimeFormatter
import connectors.HomeOfficeImmigrationStatusProxyConnector
import uk.gov.hmrc.domain.Nino

import scala.concurrent.Future

class HomeOfficeImmigrationStatusProxyServiceSpec extends ControllerSpec {

  val formatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

  val mockAuditService = mock(classOf[AuditService])
  val mockConnector = mock(classOf[HomeOfficeImmigrationStatusProxyConnector])

  override protected def beforeEach(): Unit = {
    reset(mockAuditService)
    reset(mockConnector)
    super.beforeEach()
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuditService].toInstance(mockAuditService),
      bind[HomeOfficeImmigrationStatusProxyConnector].toInstance(mockConnector)
    )
    .build()

  lazy val sut: HomeOfficeImmigrationStatusProxyService =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyService]

  val testDate = LocalDate.now
  val formModel = StatusCheckByNinoFormModel(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")
  val statusRequest = formModel.toRequest(6)
  implicit val conf = appConfig

  "statusPublicFundsByNino" should {
    "only access the audit service when the call downstream was successful" in {
      when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
      val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
      val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))
      when(mockConnector.statusPublicFundsByNino(any())(any(), any())).thenReturn(Future.successful(result))

      await(sut.statusPublicFundsByNino(formModel))
      verify(mockAuditService).auditEvent(any(), any(), any())(any(), any(), any())
    }

    "don't access the audit service when the call downstream was not successful" in {
      when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
      when(mockConnector.statusPublicFundsByNino(any())(any(), any()))
        .thenReturn(Future.failed(new Exception("It went wrong")))

      intercept[Exception](await(sut.statusPublicFundsByNino(formModel)))
      verify(mockAuditService, never).auditEvent(any(), any(), any())(any(), any(), any())
    }

    "not fail if the audit call fails" in {
      when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.failed(new Exception("It went wrong")))
      val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
      val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))
      when(mockConnector.statusPublicFundsByNino(any())(any(), any())).thenReturn(Future.successful(result))
      await(sut.statusPublicFundsByNino(formModel)) mustEqual result
    }
  }

  "auditResult" should {
    "audit a SuccessfulRequest" when {

      "the response from the home office is successful with no statuses" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)

        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "fullName"    -> "Damon Albarn",
          "dateOfBirth" -> testDate,
          "nationality" -> "GBR"
        )

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(SuccessfulRequest), refEq("StatusCheckRequest"), refEq(expectedDetails))(
            any(),
            any(),
            any())
      }

      "the response from the home office is successful with astatus" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)

        val immigrationStatuses = List(
          ImmigrationStatus(
            statusStartDate = LocalDate.parse("17/06/2021", formatter),
            statusEndDate = Some(LocalDate.parse("19/09/2021", formatter)),
            productType = "EUS",
            immigrationStatus = "ILR",
            noRecourseToPublicFunds = false
          )
        )
        val statusCheckResult = StatusCheckResult("Liam Fray", testDate, "FRA", immigrationStatuses)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "fullName"                 -> "Liam Fray",
          "dateOfBirth"              -> testDate,
          "nationality"              -> "FRA",
          "productType1"             -> "EUS",
          "immigrationStatus1"       -> "ILR",
          "noRecourseToPublicFunds1" -> false,
          "statusStartDate1"         -> LocalDate.parse("17/06/2021", formatter),
          "statusEndDate1"           -> Some(LocalDate.parse("19/09/2021", formatter)),
        )

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(SuccessfulRequest), refEq("StatusCheckRequest"), refEq(expectedDetails))(
            any(),
            any(),
            any())
      }

      "the response from the home office is successful with status" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)

        val immigrationStatuses = List(
          ImmigrationStatus(
            statusStartDate = LocalDate.parse("17/06/2021", formatter),
            statusEndDate = Some(LocalDate.parse("19/09/2021", formatter)),
            productType = "STUDY",
            immigrationStatus = "LTR",
            noRecourseToPublicFunds = true
          ),
          ImmigrationStatus(
            statusStartDate = LocalDate.parse("20/03/2019", formatter),
            statusEndDate = Some(LocalDate.parse("19/09/2019", formatter)),
            productType = "STUDY",
            immigrationStatus = "LTE",
            noRecourseToPublicFunds = false
          )
        )
        val statusCheckResult = StatusCheckResult("Jarvis Cocker", testDate, "ITA", immigrationStatuses)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "fullName"                 -> "Jarvis Cocker",
          "dateOfBirth"              -> testDate,
          "nationality"              -> "ITA",
          "productType1"             -> "STUDY",
          "immigrationStatus1"       -> "LTR",
          "noRecourseToPublicFunds1" -> true,
          "statusStartDate1"         -> LocalDate.parse("17/06/2021", formatter),
          "statusEndDate1"           -> Some(LocalDate.parse("19/09/2021", formatter)),
          "productType2"             -> "STUDY",
          "immigrationStatus2"       -> "LTE",
          "noRecourseToPublicFunds2" -> false,
          "statusStartDate2"         -> LocalDate.parse("20/03/2019", formatter),
          "statusEndDate2"           -> Some(LocalDate.parse("19/09/2019", formatter)),
        )

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(SuccessfulRequest), refEq("StatusCheckRequest"), refEq(expectedDetails))(
            any(),
            any(),
            any())
      }

    }

    "audit a NotFoundResponse" when {
      "the connector returns a StatusCheckNotFound" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
        val result = Left(StatusCheckNotFound)
        val expectedDetails = Seq("statusCode" -> StatusCheckNotFound.statusCode, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(MatchNotFound), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }
    }

    "audit a DownstreamError" when {
      "the connector returns a StatusCheckBadRequest" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
        val result = Left(StatusCheckBadRequest)
        val expectedDetails = Seq("statusCode" -> StatusCheckBadRequest.statusCode, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(DownstreamError), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }

      "the connector returns a StatusCheckConflict" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.unit)
        val result = Left(StatusCheckConflict)
        val expectedDetails = Seq("statusCode" -> StatusCheckConflict.statusCode, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(DownstreamError), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }

      "the connector returns a StatusCheckInternalServerError" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
        val result = Left(StatusCheckInternalServerError)
        val expectedDetails =
          Seq("statusCode" -> StatusCheckInternalServerError.statusCode, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(DownstreamError), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }

      "the connector returns a StatusCheckInvalidResponse" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
        val result = Left(StatusCheckInvalidResponse)
        val expectedDetails = Seq("statusCode" -> StatusCheckInvalidResponse.statusCode, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(DownstreamError), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }

      "the connector returns a OtherErrorResponse" in {
        when(mockAuditService.auditEvent(any(), any(), any())(any(), any(), any())).thenReturn(Future.unit)
        val TEAPOT = 418
        val result = Left(OtherErrorResponse(TEAPOT))
        val expectedDetails = Seq("statusCode" -> TEAPOT, "requestBody" -> request.body)

        sut.auditResult(result)
        verify(mockAuditService)
          .auditEvent(refEq(DownstreamError), refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }
    }

  }

}
