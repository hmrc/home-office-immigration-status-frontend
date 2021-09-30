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

package uk.gov.hmrc.homeofficeimmigrationstatus.services

import com.google.inject.Singleton

import javax.inject.Inject
import play.api.mvc.Request
import uk.gov.hmrc.homeofficeimmigrationstatus.services
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object HomeOfficeImmigrationStatusFrontendEvent extends Enumeration {
  val HomeOfficeImmigrationStatusFrontendSomethingHappened: services.HomeOfficeImmigrationStatusFrontendEvent.Value =
    Value
  type HomeOfficeImmigrationStatusFrontendEvent = Value
}

@Singleton
class AuditService @Inject()(val auditConnector: AuditConnector) {

  import HomeOfficeImmigrationStatusFrontendEvent._

  private[services] def auditEvent(
    event: HomeOfficeImmigrationStatusFrontendEvent,
    transactionName: String,
    details: Seq[(String, Any)] = Seq.empty)(
    implicit hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext): Future[Unit] =
    send(createEvent(event, transactionName, details: _*))

  private[services] def createEvent(
    event: HomeOfficeImmigrationStatusFrontendEvent,
    transactionName: String,
    details: (String, Any)*)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): DataEvent = {

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(
      auditSource = "home-office-immigration-status-frontend",
      auditType = event.toString,
      tags = tags,
      detail = detail)
  }

  private[services] def send(events: DataEvent*)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    Future {
      events.foreach { event =>
        Try(auditConnector.sendEvent(event))
      }
    }

}
