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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import play.api.Logger
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import play.api.Environment
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import config.AppConfig
import support.CallOps

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(
  val env: Environment,
  override val authConnector: AuthConnector,
  appConfig: AppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthAction with AuthorisedFunctions with AuthRedirects {

  val config = appConfig.configuration

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val authPredicate = getPredicate

    authorised(authPredicate)
      .retrieve(credentials and allEnrolments) {
        case Some(Credentials(authProviderId, _)) ~ enrollments =>
          val userRoles = enrollments.enrolments.map(_.key).mkString("[", ",", "]")
          Logger(getClass).info(s"User $authProviderId has been authorized with $userRoles")
          block(request)

        case None ~ _ =>
          Future.successful(Forbidden)
      }
      .recover(handleFailure(request))
  }

  def handleFailure(implicit request: Request[_]): PartialFunction[Throwable, Result] = {

    case _: AuthorisationException ⇒
      val continueUrl = CallOps.localFriendlyUrl(env, config)(request.uri, request.host)
      toStrideLogin(continueUrl)
  }

  def getPredicate =
    if (appConfig.authorisedStrideGroup == "ANY") AuthProviders(PrivilegedApplication)
    else Enrolment(appConfig.authorisedStrideGroup) and AuthProviders(PrivilegedApplication)

}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[Request, AnyContent]
