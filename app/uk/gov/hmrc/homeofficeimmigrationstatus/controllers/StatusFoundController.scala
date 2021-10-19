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
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.IdentifierAction
import uk.gov.hmrc.homeofficeimmigrationstatus.connectors.HomeOfficeImmigrationStatusProxyConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StatusFoundController @Inject()(
  identify: IdentifierAction,
  override val messagesApi: MessagesApi,
  override val config: Configuration,
  val actionBuilder: DefaultActionBuilder,
  val authConnector: AuthConnector,
  val env: Environment,
  homeOfficeImmigrationStatusProxyConnector: HomeOfficeImmigrationStatusProxyConnector,
  controllerComponents: MessagesControllerComponents,
  statusFoundPage: StatusFoundPage,
)(implicit val appConfig: AppConfig)
    extends FrontendController(controllerComponents) with I18nSupport with AuthActions {

  val onPageLoad: Action[AnyContent] =
    (identify) { implicit request =>
      val result = ???
      val query = ???
      Ok(
        statusFoundPage(
          StatusFoundPageContext(query, result, routes.HomeOfficeImmigrationStatusFrontendController.onPageLoad)))
    }

}
