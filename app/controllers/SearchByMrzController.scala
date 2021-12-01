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

import config.AppConfig
import controllers.actions.AccessAction
import forms.SearchByMRZForm
import models.{FormQueryModel, MrzSearchFormModel}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.SearchByMrzView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SearchByMrzController @Inject()(
  access: AccessAction,
  override val messagesApi: MessagesApi,
  view: SearchByMrzView,
  sessionCacheService: SessionCacheService,
  formProvider: SearchByMRZForm,
  cc: MessagesControllerComponents
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(cc) with I18nSupport {

  val onPageLoad: Action[AnyContent] =
    access.async { implicit request =>
      if (appConfig.documentSearchFeatureEnabled) {
        sessionCacheService.get.map { result =>
          val form = result match {
            case Some(FormQueryModel(_, formModel: MrzSearchFormModel, _)) => formProvider().fill(formModel)
            case _                                                         => formProvider()
          }
          Ok(view(form))
        }
      } else {
        Future.successful(Redirect(routes.LandingController.onPageLoad))
      }
    }

  val onSubmit: Action[AnyContent] =
    access.async { implicit request =>
      if (appConfig.documentSearchFeatureEnabled) {
        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
            query =>
              for {
                _ <- sessionCacheService.set(query)
              } yield Redirect(routes.StatusResultController.onPageLoad)
          )
      } else {
        Future.successful(Redirect(routes.LandingController.onPageLoad))
      }
    }

}
