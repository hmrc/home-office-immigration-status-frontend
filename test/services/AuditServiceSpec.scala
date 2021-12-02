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
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import models._
import models.HomeOfficeError._
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

  "constructDetails" should {
    "create an StatusCheckAuditDetail with response" when {
      "a result is passed in" in {
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val statusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
        val result = StatusCheckResponse("CorrelationId", statusCheckResult)

        val expectedDetails = StatusCheckSuccessAuditDetail(200, search, result)

        sut.constructDetails(search, Right(result)) mustEqual expectedDetails
      }
    }

    "create an StatusCheckAuditDetail with error" when {
      "a result is passed in" in {
        val search = MrzSearch(
          "documentType",
          "documentNumber",
          LocalDate.now,
          "nationality",
          StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
        )

        val result = StatusCheckNotFound("Nothing to see here")

        val expectedDetails = StatusCheckFailureAuditDetail(404, search, "Nothing to see here")

        sut.constructDetails(search, Left(result)) mustEqual expectedDetails
      }
    }
  }

  "auditStatusCheckEvent" should {
    "call auditConnector.send" in {
      doNothing().when(mockAuditConnector).sendExplicitAudit(any[String](), any[JsObject]())(any(), any())

      val search = MrzSearch(
        "documentType",
        "documentNumber",
        LocalDate.now,
        "nationality",
        StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
      )

      val result = StatusCheckNotFound("Nothing to see here")

      val expectedDetails = StatusCheckFailureAuditDetail(404, search, "Nothing to see here")

      sut.auditStatusCheckEvent(search, Left(result))

      verify(mockAuditConnector)
        .sendExplicitAudit(refEq("StatusCheckRequest"), refEq(expectedDetails))(any(), any(), any())

    }

  }

}
