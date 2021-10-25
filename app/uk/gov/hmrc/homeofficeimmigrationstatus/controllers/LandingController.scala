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

import play.api.mvc._
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.AuthAction
import uk.gov.hmrc.homeofficeimmigrationstatus.repositories.SessionCacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LandingController @Inject()(
  authorise: AuthAction,
  controllerComponents: MessagesControllerComponents,
  sessionCacheRepository: SessionCacheRepository
)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) {

  val onPageLoad: Action[AnyContent] = authorise { implicit request =>
    sessionCacheRepository.removeById(request.session.apply("id"))
    Redirect(routes.StatusCheckByNinoController.onPageLoad)
      .removingFromSession("query")
  }
}
