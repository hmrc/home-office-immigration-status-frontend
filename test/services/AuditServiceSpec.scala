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

import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter

import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, refEq}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import models._
import models.HomeOfficeError._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.audit.AuditExtensions._
import utils.NinoGenerator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends PlaySpec {

  val mockAuditConnector = mock(classOf[AuditConnector])
  val sut = new AuditServiceImpl(mockAuditConnector)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc = HeaderCarrier(sessionId = Some(SessionId("123")))

  val testDate = LocalDate.now
  val formatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

  "detailsFromResult" should {
    "populate the seq" when {

      "the response from the home office is successful with no statuses" in {
        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "fullName"    -> "Damon Albarn",
          "dateOfBirth" -> testDate.toString,
          "nationality" -> "GBR"
        )

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the response from the home office is successful with a status" in {
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
          "dateOfBirth"              -> testDate.toString,
          "nationality"              -> "FRA",
          "productType1"             -> "EUS",
          "immigrationStatus1"       -> "ILR",
          "noRecourseToPublicFunds1" -> false,
          "statusStartDate1"         -> LocalDate.parse("17/06/2021", formatter),
          "statusEndDate1"           -> Some(LocalDate.parse("19/09/2021", formatter)),
        )

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the response from the home office is successful with status" in {
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
          "dateOfBirth"              -> testDate.toString,
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

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a StatusCheckNotFound" in {
        val error = StatusCheckNotFound("Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a StatusCheckBadRequest" in {
        val error = StatusCheckBadRequest("Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a StatusCheckConflict" in {
        val error = StatusCheckConflict("Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a StatusCheckInternalServerError" in {
        val error = StatusCheckInternalServerError("Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a StatusCheckInvalidResponse" in {
        val error = StatusCheckInvalidResponse("Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }

      "the connector returns a OtherErrorResponse" in {
        val TEAPOT = 418
        val error = OtherErrorResponse(TEAPOT, "Some response")
        val result = Left(error)
        val expectedDetails =
          Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

        sut.detailsFromResult(result) mustEqual expectedDetails
      }
    }

  }

  "detailsFromQuery" should {

    "populate the seq" when {

      "the search is a nino search" in {
        val nino = NinoGenerator.generateNino
        val search = NinoSearch(
          nino,
          "Name",
          "Full",
          LocalDate.now.toString,
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val expectedDetails = Seq(
          "nino"        -> search.nino.toString,
          "givenName"   -> search.givenName,
          "familyName"  -> search.familyName,
          "dateOfBirth" -> search.dateOfBirth
        )

        sut.detailsFromQuery(search) mustEqual expectedDetails
      }

      "the search is an mrz search" in {
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )
        val expectedDetails = Seq(
          "documentType"   -> search.documentType,
          "documentNumber" -> search.documentNumber,
          "dateOfBirth"    -> search.dateOfBirth.toString,
          "nationality"    -> search.nationality
        )

        sut.detailsFromQuery(search) mustEqual expectedDetails
      }
    }

  }

  "constructDetailsFrom" should {

    "populate the seq" when {

      "with a nino search and result" in {
        val nino = NinoGenerator.generateNino
        val search = NinoSearch(
          nino,
          "Damon",
          "Albarn",
          LocalDate.now.toString,
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "search" -> Seq(
            "nino"        -> search.nino.toString,
            "givenName"   -> search.givenName,
            "familyName"  -> search.familyName,
            "dateOfBirth" -> search.dateOfBirth
          ),
          "result" -> Seq(
            "fullName"    -> "Damon Albarn",
            "dateOfBirth" -> testDate.toString,
            "nationality" -> "GBR"
          )
        )

        sut.constructDetailsFrom(search, result) mustEqual expectedDetails
      }

      "with a mrz search and result" in {
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val result = Right(StatusCheckResponse("CorrelationId", statusCheckResult))

        val expectedDetails = Seq(
          "search" -> Seq(
            "documentType"   -> search.documentType,
            "documentNumber" -> search.documentNumber,
            "dateOfBirth"    -> search.dateOfBirth.toString,
            "nationality"    -> search.nationality
          ),
          "result" -> Seq(
            "fullName"    -> "Damon Albarn",
            "dateOfBirth" -> testDate.toString,
            "nationality" -> "GBR"
          )
        )

        sut.constructDetailsFrom(search, result) mustEqual expectedDetails
      }

      "with a nino search and error" in {
        val nino = NinoGenerator.generateNino
        val search = NinoSearch(
          nino,
          "Damon",
          "Albarn",
          LocalDate.now.toString,
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val error = StatusCheckInvalidResponse("Some response")
        val result = Left(error)

        val expectedDetails = Seq(
          "search" -> Seq(
            "nino"        -> search.nino.toString,
            "givenName"   -> search.givenName,
            "familyName"  -> search.familyName,
            "dateOfBirth" -> search.dateOfBirth
          ),
          "result" -> Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)
        )

        sut.constructDetailsFrom(search, result) mustEqual expectedDetails
      }

      "with a mrz search and error" in {
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val error = StatusCheckInvalidResponse("Some response")
        val result = Left(error)

        val expectedDetails = Seq(
          "search" -> Seq(
            "documentType"   -> search.documentType,
            "documentNumber" -> search.documentNumber,
            "dateOfBirth"    -> search.dateOfBirth.toString,
            "nationality"    -> search.nationality
          ),
          "result" -> Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)
        )

        sut.constructDetailsFrom(search, result) mustEqual expectedDetails
      }

    }

  }

  "getTransactionFromSearch" should {

    "return NinoStatusCheckRequest" in {
      val nino = NinoGenerator.generateNino
      val search = NinoSearch(
        nino,
        "Damon",
        "Albarn",
        LocalDate.now.toString,
        StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
      )

      sut.getTransactionFromSearch(search) mustEqual "NinoStatusCheckRequest"
    }

    "return MrzStatusCheckRequest" in {
      val search = MrzSearch(
        "documentType",
        "documentNumber",
        LocalDate.now,
        "nationality",
        StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
      )

      sut.getTransactionFromSearch(search) mustEqual "MrzStatusCheckRequest"
    }

  }

  "auditStatusCheckEvent" should {
    "call auditConnector.send" in {
      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val search = MrzSearch(
        "documentType",
        "documentNumber",
        LocalDate.now,
        "nationality",
        StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
      )

      val error = StatusCheckInvalidResponse("Some response")
      val result = Left(error)

      sut.auditStatusCheckEvent(search, result)

      verify(mockAuditConnector).sendEvent(any())(any(), any())

    }

  }

}
