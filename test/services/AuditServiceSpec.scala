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

package services

import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter

import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, refEq}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import models._
import play.api.libs.json.{JsObject}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends PlaySpec {

  val mockAuditConnector = mock(classOf[AuditConnector])
  val sut = new AuditServiceImpl(mockAuditConnector)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc = HeaderCarrier(sessionId = Some(SessionId("123")))

  val testDate = LocalDate.now
  val formatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

  val correlationId = Some("correlationId")

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

        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val response = StatusCheckSuccessfulResponse(correlationId, statusCheckResult)
        val responseWithStatus = StatusCheckResponseWithStatus(200, response)

        val expectedDetails = StatusCheckAuditDetail(200, search, response)

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

        val statusCheckResult = StatusCheckError("UNKNOWN_ERROR")
        val error = StatusCheckErrorResponse(correlationId, statusCheckResult)
        val responseWithStatus = StatusCheckResponseWithStatus(500, error)

        val expectedDetails = StatusCheckAuditDetail(500, search, error)

        sut.auditStatusCheckEvent(search, responseWithStatus)

        verify(mockAuditConnector)
          .sendExplicitAudit(refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())
      }
    }
  }

}
