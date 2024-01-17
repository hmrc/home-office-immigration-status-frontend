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

package services

import models._
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.JsObject
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends PlaySpec {

  val mockAuditConnector: AuditConnector                    = mock(classOf[AuditConnector])
  val sut                                                   = new AuditServiceImpl(mockAuditConnector)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier                            = HeaderCarrier(sessionId = Some(SessionId("123")))

  val testDate: LocalDate          = LocalDate.now
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

  val correlationId: Some[String] = Some("correlationId")

  "auditStatusCheckEvent" should {

    "call auditConnector.send" when {

      "a result is passed in" in {
        doNothing().when(mockAuditConnector).sendExplicitAudit(any[String](), any[JsObject]())(any(), any())
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val statusCheckResult  = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val response           = StatusCheckSuccessfulResponse(correlationId, statusCheckResult)
        val responseWithStatus = StatusCheckResponseWithStatus(OK, response)

        val expectedDetails = StatusCheckAuditDetail(OK, search, response)

        sut.auditStatusCheckEvent(search, responseWithStatus)

        verify(mockAuditConnector)
          .sendExplicitAudit(refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }

      "an error is passed in" in {
        doNothing().when(mockAuditConnector).sendExplicitAudit(any[String](), any[JsObject]())(any(), any())
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val statusCheckResult  = StatusCheckError("UNKNOWN_ERROR")
        val error              = StatusCheckErrorResponse(correlationId, statusCheckResult)
        val responseWithStatus = StatusCheckResponseWithStatus(INTERNAL_SERVER_ERROR, error)

        val expectedDetails = StatusCheckAuditDetail(INTERNAL_SERVER_ERROR, search, error)

        sut.auditStatusCheckEvent(search, responseWithStatus)

        verify(mockAuditConnector)
          .sendExplicitAudit(refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }
    }
  }

}
