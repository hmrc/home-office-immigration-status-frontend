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

import play.api.Logger
import play.api.mvc.Results.Forbidden
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.homeofficesettledstatus.support.CallOps
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

trait AuthActions extends AuthorisedFunctions with AuthRedirects {

  protected def authorisedWithStrideGroup[A](authorisedStrideGroup: String)(body: String => Future[Result])(
    implicit
    request: Request[A],
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[Result] = {
    val authPredicate =
      if (authorisedStrideGroup == "ANY") AuthProviders(PrivilegedApplication)
      else Enrolment(authorisedStrideGroup) and AuthProviders(PrivilegedApplication)
    authorised(authPredicate)
      .retrieve(credentials and allEnrolments) {
        case Some(Credentials(authProviderId, _)) ~ _ =>
          body(authProviderId)

        case None ~ enrollments =>
          val userRoles = enrollments.enrolments.map(_.key).mkString("[", ",", "]")
          Logger(getClass).error(s"User not authorized, expected $authorisedStrideGroup, but got $userRoles")
          Future.successful(Forbidden)
      }
      .recover(handleFailure)
  }

  def handleFailure(implicit request: Request[_]): PartialFunction[Throwable, Result] = {

    case _: AuthorisationException ⇒
      val continueUrl = CallOps.localFriendlyUrl(env, config)(request.uri, request.host)
      toStrideLogin(continueUrl)
  }

}
