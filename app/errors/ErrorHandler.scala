/*
 * Copyright 2025 HM Revenue & Customs
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

package errors

import com.google.inject.name.Named
import config.AppConfig
import controllers.actions.AuthRedirects
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.{Configuration, Environment, Logging}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.http.{JsValidationException, NotFoundException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.{ExternalErrorPage, ShutteringPage, error_template}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (
  val env: Environment,
  val messagesApi: MessagesApi,
  val auditConnector: AuditConnector,
  externalErrorPage: ExternalErrorPage,
  errorTemplate: error_template,
  shutteringPage: ShutteringPage,
  @Named("appName") val appName: String,
  appConfig: AppConfig,
  val config: Configuration
)(implicit val ec: ExecutionContext)
    extends FrontendErrorHandler
    with AuthRedirects
    with ErrorAuditing
    with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    auditClientError(request, statusCode, message)
    if (appConfig.shuttered) {
      given r: Request[String] = Request(request, "")
      Future.successful(ServiceUnavailable(shutteringPage()))
    } else {
      super.onClientError(request, statusCode, message)
    }
  }

  override def resolveError(request: RequestHeader, exception: Throwable): Future[Result] = {
    auditServerError(request, exception)
    implicit val r: Request[String] = Request(request, "")
    exception match {
      case _: NoActiveSession =>
        Future(toGGLogin(if (appConfig.isDevEnv) s"http://${request.host}${request.uri}" else s"${request.uri}"))
      case _: InsufficientEnrolments =>
        Future(Forbidden)
      case e =>
        logger.error(s"[ErrorHandler][resolveError] ${e.getMessage}", e)
        Future(InternalServerError(externalErrorPage()))
    }
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    request: RequestHeader
  ): Future[Html] =
    Future(errorTemplate(pageTitle, heading, message, None))
}

object EventTypes {

  val TransactionFailureReason: String = "transactionFailureReason"
  val ServerInternalError: String      = "ServerInternalError"
  val ResourceNotFound: String         = "ResourceNotFound"
  val ServerValidationError: String    = "ServerValidationError"
}

trait ErrorAuditing extends HttpAuditEvent {

  import EventTypes._

  def auditConnector: AuditConnector

  private val unexpectedError = "Unexpected error"
  private val notFoundError   = "Resource Endpoint Not Found"
  private val badRequestError = "Request bad format exception"

  def auditServerError(request: RequestHeader, ex: Throwable)(implicit ec: ExecutionContext): Unit = {
    val eventType = ex match {
      case _: NotFoundException     => ResourceNotFound
      case _: JsValidationException => ServerValidationError
      case _                        => ServerInternalError
    }
    val transactionName = ex match {
      case _: NotFoundException => notFoundError
      case _                    => unexpectedError
    }
    auditConnector.sendEvent(
      dataEvent(eventType, transactionName, request, Map(TransactionFailureReason -> ex.getMessage))(
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      )
    )
  }

  def auditClientError(request: RequestHeader, statusCode: Int, message: String)(implicit ec: ExecutionContext): Unit =
    statusCode match {
      case NOT_FOUND =>
        auditConnector.sendEvent(
          dataEvent(ResourceNotFound, notFoundError, request, Map(TransactionFailureReason -> message))(
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          )
        )
      case BAD_REQUEST =>
        auditConnector.sendEvent(
          dataEvent(ServerValidationError, badRequestError, request, Map(TransactionFailureReason -> message))(
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          )
        )
      case _ => ()
    }
}
