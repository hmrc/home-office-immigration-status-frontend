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

import com.google.inject.Singleton
import com.google.inject.{ImplementedBy, Inject}
import models.{HomeOfficeError, MrzSearch, NinoSearch, Search, StatusCheckResponse}
import play.api.mvc.Request
import services.HomeOfficeImmigrationStatusFrontendEvent.StatusCheckRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AuditServiceImpl @Inject()(val auditConnector: AuditConnector) extends AuditService {

  def auditEvent(
    event: HomeOfficeImmigrationStatusFrontendEvent,
    transactionName: String,
    details: Seq[(String, Any)] = Seq.empty)(
    implicit hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext): Future[Unit] =
    send(createEvent(event, transactionName, details: _*))

  private def createEvent(
    event: HomeOfficeImmigrationStatusFrontendEvent,
    transactionName: String,
    details: (String, Any)*)(implicit hc: HeaderCarrier, request: Request[Any]): DataEvent = {

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(
      auditSource = "home-office-immigration-status-frontend",
      auditType = event.toString,
      tags = tags,
      detail = detail)
  }

  private def send(events: DataEvent*)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    Future {
      events.foreach { event =>
        println(event.toString)
        Try(auditConnector.sendEvent(event))
      }
    }

  def auditStatusCheckEvent(search: Search, result: Either[HomeOfficeError, StatusCheckResponse])(
    implicit hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext): Future[Unit] = {

    val auditTransaction = getTransactionFromSearch(search)

    val details = constructDetailsFrom(search, result)

    auditEvent(StatusCheckRequest, auditTransaction, details)
  }

  private[services] def constructDetailsFrom(
    search: Search,
    result: Either[HomeOfficeError, StatusCheckResponse]): Seq[(String, Any)] = {
    val resultDetails = detailsFromResult(result)
    val searchDetails = detailsFromQuery(search)
    Seq(
      "search" -> searchDetails,
      "result" -> resultDetails
    )
  }

  private[services] def getTransactionFromSearch(search: Search): String = search match {
    case _: NinoSearch => "NinoStatusCheckRequest"
    case _: MrzSearch  => "MrzStatusCheckRequest"
  }

  private[services] def detailsFromResult(result: Either[HomeOfficeError, StatusCheckResponse]): Seq[(String, Any)] =
    result match {
      case Right(response) => detailsFromStatusCheckResponse(response)
      case Left(error)     => detailsFromError(error)
    }

  private[services] def detailsFromQuery(search: Search): Seq[(String, Any)] =
    search match {
      case s: NinoSearch => detailsFromNinoQuery(s)
      case s: MrzSearch  => detailsFromMrzQuery(s)
    }

  private def detailsFromNinoQuery(search: NinoSearch): Seq[(String, Any)] =
    Seq(
      "nino"        -> search.nino.toString,
      "givenName"   -> search.givenName,
      "familyName"  -> search.familyName,
      "dateOfBirth" -> search.dateOfBirth
    )

  private def detailsFromMrzQuery(search: MrzSearch): Seq[(String, Any)] =
    Seq(
      "documentType"   -> search.documentType,
      "documentNumber" -> search.documentNumber,
      "dateOfBirth"    -> search.dateOfBirth.toString,
      "nationality"    -> search.nationality
    )

  private def detailsFromStatusCheckResponse(response: StatusCheckResponse): Seq[(String, Any)] = {

    val statusDetails = response.result.statusesSortedByDate.zipWithIndex.map {
      case (status, idx) =>
        val count = idx + 1
        Seq(
          s"productType$count"             -> status.productType,
          s"immigrationStatus$count"       -> status.immigrationStatus,
          s"noRecourseToPublicFunds$count" -> status.noRecourseToPublicFunds,
          s"statusStartDate$count"         -> status.statusStartDate,
          s"statusEndDate$count"           -> status.statusEndDate
        )
    }

    val baseDetails = Seq(
      "fullName"    -> response.result.fullName,
      "dateOfBirth" -> response.result.dateOfBirth.toString,
      "nationality" -> response.result.nationality
    )

    (baseDetails +: statusDetails).flatten
  }

  private def detailsFromError(error: HomeOfficeError): Seq[(String, Any)] =
    Seq("statusCode" -> error.statusCode, "error" -> error.responseBody)

}

@ImplementedBy(classOf[AuditServiceImpl])
trait AuditService {
  def auditStatusCheckEvent(search: Search, result: Either[HomeOfficeError, StatusCheckResponse])(
    implicit hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext): Future[Unit]
}
