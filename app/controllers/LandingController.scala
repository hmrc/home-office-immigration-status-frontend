/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.mvc._
import controllers.actions.AccessAction
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import javax.inject.{Inject, Singleton}
import models.{MrzSearchFormModel, NinoSearchFormModel}
import scala.concurrent.ExecutionContext

@Singleton
class LandingController @Inject() (
  access: AccessAction,
  controllerComponents: MessagesControllerComponents,
  sessionCacheService: SessionCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) {

  val onPageLoad: Action[AnyContent] = access.async { implicit request =>
    sessionCacheService.get.map {
      case Some(_: NinoSearchFormModel) =>
        Redirect(routes.SearchByNinoController.onPageLoad(true))
      case Some(_: MrzSearchFormModel) =>
        Redirect(routes.SearchByMrzController.onPageLoad(true))
      case _ => Redirect(routes.SearchByNinoController.onPageLoad())
    }
  }
}
