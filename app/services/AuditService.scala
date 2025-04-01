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

package services

import com.google.inject.Singleton
import com.google.inject.{ImplementedBy, Inject}
import models.{Search, StatusCheckAuditDetail, StatusCheckResponseWithStatus}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

@Singleton
class AuditServiceImpl @Inject() (val auditConnector: AuditConnector) extends AuditService {

  def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(implicit
    hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext
  ): Unit = {

    val AUDIT_TYPE = "StatusCheckRequest"

    val details = StatusCheckAuditDetail(result.statusCode, search, result.statusCheckResponse)
    auditConnector.sendExplicitAudit(AUDIT_TYPE, details)
  }

}

@ImplementedBy(classOf[AuditServiceImpl])
trait AuditService {
  def auditStatusCheckEvent(search: Search, result: StatusCheckResponseWithStatus)(implicit
    hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext
  ): Unit
}
