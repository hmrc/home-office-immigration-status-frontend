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
import controllers.actions.AccessAction
import forms.SearchByNinoForm
import models.NinoSearchFormModel
import views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import services.SessionCacheService
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchByNinoController @Inject()(
  access: AccessAction,
  override val messagesApi: MessagesApi,
  controllerComponents: MessagesControllerComponents,
  formProvider: SearchByNinoForm,
  searchByNinoView: SearchByNinoView,
  sessionCacheService: SessionCacheService
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport {

  def onPageLoad(clearForm: Boolean): Action[AnyContent] =
    access.async { implicit request =>
      if (clearForm) {
        clearStoredRequestAndShowEmptyForm
      } else {
        composeFormWithStoredRequest
      }
    }

  private def composeFormWithStoredRequest(implicit request: Request[_]): Future[Result] = sessionCacheService.get.map {
    result =>
      val form = result match {
        case Some(formModel: NinoSearchFormModel) => formProvider().fill(formModel)
        case _                                    => formProvider()
      }
      Ok(searchByNinoView(form))
  }

  private def clearStoredRequestAndShowEmptyForm(implicit request: Request[_]): Future[Result] =
    for {
      _ <- sessionCacheService.delete
    } yield Ok(searchByNinoView(formProvider()))

  val onSubmit: Action[AnyContent] =
    access.async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(searchByNinoView(formWithErrors))),
          query =>
            for {
              _ <- sessionCacheService.set(query)
            } yield Redirect(routes.StatusResultController.onPageLoad)
        )
    }

}
