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
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.AuthAction
import uk.gov.hmrc.homeofficeimmigrationstatus.forms.StatusCheckByNinoFormProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

@Singleton
class StatusCheckByNinoController @Inject()(
  authorise: AuthAction,
  override val messagesApi: MessagesApi,
  controllerComponents: MessagesControllerComponents,
  formProvider: StatusCheckByNinoFormProvider,
  statusCheckByNinoPage: StatusCheckByNinoPage
)(implicit val appConfig: AppConfig)
    extends FrontendController(controllerComponents) with I18nSupport {

  val onPageLoad: Action[AnyContent] =
    (authorise) { implicit request =>
      val maybeQuery: Option[StatusCheckByNinoRequest] =
        request.session.get("query").map(Json.parse).flatMap(_.asOpt[StatusCheckByNinoRequest])
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
    (authorise) { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors =>
            BadRequest(statusCheckByNinoPage(formWithErrors, routes.StatusCheckByNinoController.onSubmit)),
          query => {
            Redirect(routes.StatusResultController.onPageLoad).addingToSession("query" -> Json.toJson(query).toString)
          }
        )
    }

}
