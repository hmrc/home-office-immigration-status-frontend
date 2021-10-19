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

import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.connectors.HomeOfficeImmigrationStatusProxyConnector
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.IdentifierAction
import uk.gov.hmrc.homeofficeimmigrationstatus.forms.StatusCheckByNinoFormProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.{LocalDate, ZoneId}

@Singleton
class StatusCheckByNinoController @Inject()(
  identify: IdentifierAction,
  override val messagesApi: MessagesApi,
  val actionBuilder: DefaultActionBuilder,
  val authConnector: AuthConnector,
  val env: Environment,
  homeOfficeImmigrationStatusProxyConnector: HomeOfficeImmigrationStatusProxyConnector,
  controllerComponents: MessagesControllerComponents,
  formProvider: StatusCheckByNinoFormProvider,
  statusCheckByNinoPage: StatusCheckByNinoPage
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport {

  val onPageLoad: Action[AnyContent] =
    (identify) { implicit request =>
      val maybeQuery: Option[StatusCheckByNinoRequest] = ???
      Ok(
        statusCheckByNinoPage(
          maybeQuery
            .map(query => formProvider().fill(query))
            .getOrElse(formProvider()),
          routes.StatusCheckByNinoController.onSubmit
        )
      )
    }

  val onSubmit: Action[AnyContent] =
    (identify).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(statusCheckByNinoPage(formWithErrors, routes.StatusCheckByNinoController.onSubmit))),
          query => {
            // This should set the mongo state with the request and the response/error
            val enrichedQuery = enrichWithDateRange(query, appConfig.defaultQueryTimeRangeInMonths)
            homeOfficeImmigrationStatusProxyConnector.statusPublicFundsByNino(enrichedQuery).map {
              case StatusCheckResponse(correlationId, Some(error), _) =>
                Redirect(routes.StatusCheckFailureController.onPageLoad)
              case StatusCheckResponse(correlationId, _, Some(result)) if !result.statuses.isEmpty =>
                Redirect(routes.StatusFoundController.onPageLoad)
              case StatusCheckResponse(correlationId, _, _) =>
                Redirect(routes.StatusNotAvailableController.onPageLoad)
            }
          }
        )
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
