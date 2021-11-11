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

import java.net.URL
import java.util.UUID
import javax.inject.{Inject, Singleton}
import models.{HomeOfficeError, StatusCheckByNinoFormModel, StatusCheckByNinoRequest, StatusCheckResponse}
import play.api.mvc.Request
import services.HomeOfficeImmigrationStatusFrontendEvent._
import uk.gov.hmrc.http.HeaderCarrier
import models.HomeOfficeError._
import connectors.HomeOfficeImmigrationStatusProxyConnector
import config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeImmigrationStatusProxyService @Inject()(
  connector: HomeOfficeImmigrationStatusProxyConnector,
  auditService: AuditService) {

  private val auditTransaction = "StatusCheckRequest"
  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  def statusPublicFundsByNino(query: StatusCheckByNinoFormModel)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    request: Request[Any],
    appConfig: AppConfig): Future[Either[HomeOfficeError, StatusCheckResponse]] = {

    val headerCarrier = hc.withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString)
    val checkRequest = query.toRequest(appConfig.defaultQueryTimeRangeInMonths)

    val response = connector.statusPublicFundsByNino(checkRequest)(headerCarrier, ec)
    response.map { result =>
      auditResult(result)
      result
    }
  }

  def auditResult(result: Either[HomeOfficeError, StatusCheckResponse])(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    request: Request[Any]): Future[Unit] =
    result match {
      case Right(response) =>
        val details = detailsFromStatusCheckResponse(response)
        auditService.auditEvent(SuccessfulRequest, auditTransaction, details)
      case Left(StatusCheckNotFound) =>
        auditService.auditEvent(MatchNotFound, auditTransaction, detailsFromError(StatusCheckNotFound))
      case Left(error) =>
        auditService.auditEvent(DownstreamError, auditTransaction, detailsFromError(error))
    }

  def detailsFromStatusCheckResponse(response: StatusCheckResponse): Seq[(String, Any)] = {

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
      "dateOfBirth" -> response.result.dateOfBirth,
      "nationality" -> response.result.nationality
    )

    (baseDetails +: statusDetails).flatten
  }

  def detailsFromError(error: HomeOfficeError)(implicit request: Request[Any]): Seq[(String, Any)] =
    Seq("statusCode" -> error.statusCode, "requestBody" -> request.body)

}
