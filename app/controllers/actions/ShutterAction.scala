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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import models.IsShutteredFlag
import play.api.mvc.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.*
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import views.html.ShutteringPage

import scala.concurrent.{ExecutionContext, Future}

class ShutterActionImpl @Inject() (
  val messagesApi: MessagesApi,
  val parser: BodyParsers.Default,
  shutteringPage: ShutteringPage
)(implicit val executionContext: ExecutionContext, featureFlagService: FeatureFlagService)
    extends ShutterAction
    with I18nSupport {

  def filter[A](implicit request: Request[A]): Future[Option[Result]] =
    for {
      isShuttered <- featureFlagService.get(IsShutteredFlag).map(_.isEnabled)
    } yield
      if (isShuttered) {
        Some(ServiceUnavailable(shutteringPage()))
      } else {
        None
      }
}

@ImplementedBy(classOf[ShutterActionImpl])
trait ShutterAction extends ActionFilter[Request]
