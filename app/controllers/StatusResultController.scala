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
import services.HomeOfficeImmigrationStatusProxyService
import controllers.actions.AccessAction
import models._
import models.HomeOfficeError._
import views._
import views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import services.SessionCacheService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
import errors.ErrorHandler

@Singleton
class StatusResultController @Inject()(
  access: AccessAction,
  override val messagesApi: MessagesApi,
  homeOfficeService: HomeOfficeImmigrationStatusProxyService,
  controllerComponents: MessagesControllerComponents,
  statusFoundPage: StatusFoundPage,
  statusCheckFailurePage: StatusCheckFailurePage,
  statusNotAvailablePage: StatusNotAvailablePage,
  multipleMatchesFoundPage: MultipleMatchesFoundPage,
  sessionCacheService: SessionCacheService,
  externalErrorPage: ExternalErrorPage,
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with Logging {

  val onPageLoad: Action[AnyContent] =
    access.async { implicit request =>
      sessionCacheService.get.flatMap {
        case Some(FormQueryModel(_, query, _)) =>
          homeOfficeService
            .search(query)
            .map(result => result.fold(handleError(query), displaySuccessfulResult(query)))
        case None =>
          Future.successful(Redirect(routes.StatusCheckByNinoController.onPageLoad))
      }
    }

  private def handleError(query: SearchFormModel)(error: HomeOfficeError)(
    implicit request: Request[AnyContent]): Result =
    error match {
      case StatusCheckConflict(_) => Ok(multipleMatchesFoundPage(query, query.isInstanceOf[NinoSearchFormModel]))
      case StatusCheckNotFound(_) => Ok(statusCheckFailurePage(query))
      case _                      => InternalServerError(externalErrorPage())
    }

  private def displaySuccessfulResult(query: SearchFormModel)(response: StatusCheckResponse)(
    implicit request: Request[AnyContent]): Result =
    response.result.statuses match {
      case Nil =>
        logger.info(s"Match found with no statuses - CorrelationId: ${response.correlationId}")
        Ok(statusNotAvailablePage(StatusNotAvailablePageContext(query, routes.LandingController.onPageLoad)))
      case _ =>
        Ok(statusFoundPage(StatusFoundPageContext(query, response.result, routes.LandingController.onPageLoad)))
    }

}
