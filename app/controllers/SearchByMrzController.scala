/*
 * Copyright 2022 HM Revenue & Customs
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
import models.MrzSearchFormModel
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.SearchByMrzView
import errors.ErrorHandler
import play.api.data.FormError

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SearchByMrzController @Inject()(
  access: AccessAction,
  override val messagesApi: MessagesApi,
  view: SearchByMrzView,
  sessionCacheService: SessionCacheService,
  formProvider: SearchByMRZForm,
  cc: MessagesControllerComponents,
  errorHandler: ErrorHandler
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(cc) with I18nSupport {

  def onPageLoad(clearForm: Boolean): Action[AnyContent] =
    access.async { implicit request =>
      if (appConfig.documentSearchFeatureEnabled) {
        if (clearForm) {
          clearStoredRequestAndShowEmptyForm
        } else {
          composeFormWithStoredRequest
        }
      } else {
        Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }

  private def composeFormWithStoredRequest(implicit request: Request[_]): Future[Result] = sessionCacheService.get.map {
    result =>
      val form = result match {
        case Some(formModel: MrzSearchFormModel) => formProvider().fill(formModel)
        case _                                   => formProvider()
      }
      Ok(view(form))
  }

  private def clearStoredRequestAndShowEmptyForm(implicit request: Request[_]): Future[Result] =
    for {
      _ <- sessionCacheService.delete
    } yield Ok(view(formProvider()))

  val onSubmit: Action[AnyContent] =
    access.async { implicit request =>
      if (appConfig.documentSearchFeatureEnabled) {
        formProvider()
          .bindFromRequest()
          .fold(
            form => {
              //todo move this to the form
              val dobErrorsCollated =
                if (form.errors.count(_.key.contains("dateOfBirth")) > 1) {
                  val required = form.errors.count(_.message.matches(""".*dateOfBirth.*\.required""")) == 3
                  (form.errors.filterNot(_.key.contains("dateOfBirth")) :+ FormError(
                    "dateOfBirth",
                    "error.dateOfBirth." + (if (required) "required" else "invalid-format")))
                    .foldLeft(form.discardingErrors)((acc, cur) => acc.withError(cur))
                } else form

              Future.successful(BadRequest(view(dobErrorsCollated)))
            },
            query =>
              for {
                _ <- sessionCacheService.set(query)
              } yield Redirect(routes.StatusResultController.onPageLoad)
          )
      } else {
        Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }
}
