/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.mvc.Results.Forbidden
import play.api.mvc.{Request, Result}
import play.api.{Logger, Mode}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.credentials
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

trait AuthActions extends AuthorisedFunctions with AuthRedirects {

  private lazy val isDevEnv =
    if (env.mode.equals(Mode.Test)) false
    else config.getString("run.mode").forall(Mode.Dev.toString.equals)

  protected def authorisedWithStrideGroup[A](authorisedStrideGroup: String)(
    body: String => Future[Result])(
    implicit
    request: Request[A],
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[Result] =
    authorised(Enrolment(authorisedStrideGroup) and AuthProviders(PrivilegedApplication))
      .retrieve(credentials) {
        case Some(Credentials(authProviderId, _)) => body(authProviderId)
        case None =>
          Logger.warn("No credentials found for the user")
          Future.successful(Forbidden)
      }
      .recover(handleFailure)

  def handleFailure(implicit request: Request[_]): PartialFunction[Throwable, Result] = {

    case _: NoActiveSession ⇒
      toStrideLogin(if (isDevEnv) s"http://${request.host}${request.path}" else s"${request.path}")

    case _: UnsupportedAuthProvider ⇒
      Logger.warn(s"user logged in with unsupported auth provider")
      Forbidden

    case _: InsufficientEnrolments ⇒
      Logger.warn(s"Logged in user does not have required enrolments")
      Forbidden
  }

  def withAuthorisedAsHuman[A](body: String => Future[Result])(
    implicit request: Request[A],
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[Result] = body("You are a human")

}
