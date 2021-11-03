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

package filters

import akka.stream.Materializer
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.ServiceUnavailable
import play.api.mvc._
import config.AppConfig
import views.html.error_template

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ShutteringFilter @Inject()(val messagesApi: MessagesApi, errorTemplate: error_template)(
  implicit val mat: Materializer,
  appConfig: AppConfig)
    extends Filter with I18nSupport {

  private val isServiceShuttered: Boolean = appConfig.shuttered

  private def notARequestForAnAsset(implicit rh: RequestHeader) =
    !(rh.path.startsWith("/template/") || rh.path.startsWith("/check-immigration-status/assets/"))

  override def apply(next: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    implicit val request: Request[_] = Request(rh, "")
    if (isServiceShuttered && notARequestForAnAsset)
      Future.successful(
        ServiceUnavailable(
          errorTemplate(
            Messages("shuttering.title"),
            Messages("shuttering.title"),
            Messages("global.error.500.message")
          )
        ))
    else next(rh)
  }
}