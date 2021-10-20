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

package uk.gov.hmrc.homeofficeimmigrationstatus.controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html._
import uk.gov.hmrc.homeofficeimmigrationstatus.views.StatusFoundPageContext
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.AuthAction
import uk.gov.hmrc.homeofficeimmigrationstatus.connectors.HomeOfficeImmigrationStatusProxyConnector
import uk.gov.hmrc.homeofficeimmigrationstatus.views._
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.{LocalDate, ZoneId}
import play.api.libs.json.Json

@Singleton
class StatusResultController @Inject()(
  authorise: AuthAction,
  override val messagesApi: MessagesApi,
  homeOfficeImmigrationStatusProxyConnector: HomeOfficeImmigrationStatusProxyConnector,
  controllerComponents: MessagesControllerComponents,
  statusFoundPage: StatusFoundPage,
  statusCheckFailurePage: StatusCheckFailurePage,
  statusNotAvailablePage: StatusNotAvailablePage,
  multipleMatchesFoundPage: MultipleMatchesFoundPage
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport {

  val onPageLoad: Action[AnyContent] =
    (authorise).async { implicit request =>
      val query: Option[StatusCheckByNinoRequest] =
        request.session.get("query").map(Json.parse).flatMap(_.asOpt[StatusCheckByNinoRequest])
      query match {
        case Some(req) =>
          val enrichedQuery = enrichWithDateRange(req, appConfig.defaultQueryTimeRangeInMonths)
          homeOfficeImmigrationStatusProxyConnector
            .statusPublicFundsByNino(enrichedQuery)
            .map(result => displayResults(req, result))
        case None =>
          Future.successful(Redirect(routes.StatusCheckByNinoController.onPageLoad))
      }

    }

  def displayResults(query: StatusCheckByNinoRequest, statusCheckResponse: StatusCheckResponse)(
    implicit request: Request[AnyContent]): Result =
    statusCheckResponse match {
      case StatusCheckResponse(_, Some(error), _) =>
        if (error.errCode == "ERR_CONFLICT") Ok(multipleMatchesFoundPage(query))
        else Ok(statusCheckFailurePage(query))
      case StatusCheckResponse(_, _, Some(result)) if !result.statuses.isEmpty =>
        Ok(statusFoundPage(StatusFoundPageContext(query, result, routes.LandingController.onPageLoad)))
      case _ =>
        Ok(statusNotAvailablePage(StatusNotAvailablePageContext(query, routes.LandingController.onPageLoad)))
    }

  def enrichWithDateRange(query: StatusCheckByNinoRequest, timeRangeInMonths: Int) = {
    val startDate = query.statusCheckRange
      .flatMap(_.startDate)
      .getOrElse(LocalDate.now(ZoneId.of("UTC")).minusMonths(timeRangeInMonths))
    val endDate =
      query.statusCheckRange.flatMap(_.endDate).getOrElse(LocalDate.now(ZoneId.of("UTC")))
    query.copy(statusCheckRange = Some(StatusCheckRange(Some(startDate), Some(endDate))))
  }

}
