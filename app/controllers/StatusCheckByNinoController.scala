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
import controllers.actions.AuthAction
import forms.StatusCheckByNinoFormProvider
import models.{FormQueryModel}
import views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import services.SessionCacheService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StatusCheckByNinoController @Inject()(
  authorise: AuthAction,
  override val messagesApi: MessagesApi,
  controllerComponents: MessagesControllerComponents,
  formProvider: StatusCheckByNinoFormProvider,
  statusCheckByNinoPage: StatusCheckByNinoPage,
  sessionCacheService: SessionCacheService
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport {

  val onPageLoad: Action[AnyContent] =
    (authorise).async { implicit request =>
      sessionCacheService.get.map { result =>
        val form = result match {
          case Some(FormQueryModel(_, formModel, _)) => formProvider().fill(formModel)
          case _                                     => formProvider()
        }
        Ok(statusCheckByNinoPage(form, routes.StatusCheckByNinoController.onSubmit))
      }
    }

  val onSubmit: Action[AnyContent] =
    (authorise).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(statusCheckByNinoPage(formWithErrors, routes.StatusCheckByNinoController.onSubmit))),
          query =>
            for {
              _ <- sessionCacheService.set(query)
            } yield Redirect(routes.StatusResultController.onPageLoad)
        )
    }
}
