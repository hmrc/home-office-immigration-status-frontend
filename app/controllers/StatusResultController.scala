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

package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import config.AppConfig
import connectors.HomeOfficeImmigrationStatusProxyConnector
import controllers.actions.AccessAction
import models.{FormQueryModel, StatusCheckByNinoFormModel, StatusCheckResponse}
import views._
import views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import services.SessionCacheService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

@Singleton
class StatusResultController @Inject()(
  access: AccessAction,
  override val messagesApi: MessagesApi,
  homeOfficeConnector: HomeOfficeImmigrationStatusProxyConnector,
  controllerComponents: MessagesControllerComponents,
  statusFoundPage: StatusFoundPage,
  statusCheckFailurePage: StatusCheckFailurePage,
  statusNotAvailablePage: StatusNotAvailablePage,
  multipleMatchesFoundPage: MultipleMatchesFoundPage,
  sessionCacheService: SessionCacheService
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with Logging {

  val onPageLoad: Action[AnyContent] =
    access.async { implicit request =>
      sessionCacheService.get.flatMap {
        case Some(FormQueryModel(_, query, _)) =>
          val req = query.toRequest(appConfig.defaultQueryTimeRangeInMonths) //todo move this to a service
          homeOfficeConnector
            .statusPublicFundsByNino(req)
            .map(result => displayResults(query, result))
        case None =>
          Future.successful(Redirect(routes.StatusCheckByNinoController.onPageLoad))
      }
    }

  private def displayResults(query: StatusCheckByNinoFormModel, statusCheckResponse: StatusCheckResponse)(
    implicit request: Request[AnyContent]): Result =
    statusCheckResponse match {
      case StatusCheckResponse(_, Some(error), _) =>
        if (error.errCode == "ERR_CONFLICT") Ok(multipleMatchesFoundPage(query))
        else Ok(statusCheckFailurePage(query))
      case StatusCheckResponse(_, _, Some(result)) if result.statuses.nonEmpty =>
        Ok(statusFoundPage(StatusFoundPageContext(query, result, routes.LandingController.onPageLoad)))
      case StatusCheckResponse(correlationId, _, _) =>
        logger.info(s"Match found with no statuses - CorrelationId: $correlationId")
        Ok(statusNotAvailablePage(StatusNotAvailablePageContext(query, routes.LandingController.onPageLoad)))
    }
}
