/*
 * Copyright 2023 HM Revenue & Customs
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

import config.AppConfig
import controllers.ControllerSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api._
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ErrorHandlerSpec extends ControllerSpec {

  private val mockEnvironment: Environment       = mock(classOf[Environment])
  private val mockAuditConnector: AuditConnector = mock(classOf[AuditConnector])
  private val mockAppConfig: AppConfig           = mock(classOf[AppConfig])
  private val mockConfiguration: Configuration   = mock(classOf[Configuration])

  private lazy val externalErrorPage: ExternalErrorPage = inject[ExternalErrorPage]
  private lazy val errorTemplate: error_template        = inject[error_template]
  private lazy val shutteringPage: ShutteringPage       = inject[ShutteringPage]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockConfiguration.getOptional(any())(any())).thenReturn(None)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockEnvironment)
    reset(mockAuditConnector)
    reset(mockAppConfig)
    reset(mockConfiguration)
  }

  private lazy val sut: ErrorHandler = new ErrorHandler(
    env = mockEnvironment,
    messagesApi = messagesApi,
    auditConnector = mockAuditConnector,
    externalErrorPage = externalErrorPage,
    errorTemplate = errorTemplate,
    shutteringPage = shutteringPage,
    appName = "home-office-immigration-status-frontend",
    appConfig = mockAppConfig,
    config = mockConfiguration
  )

  "ErrorHandler" when {
    ".resolveError" must {
      "redirect to GGLogin" when {
        "there is NoActiveSession and environment mode is not Dev" in {
          when(mockEnvironment.mode).thenReturn(Mode.Test)
          when(mockAppConfig.isDevEnv).thenReturn(false)

          val exception: Throwable   = BearerTokenExpired()
          val result: Future[Result] = Future.successful(sut.resolveError(request, exception))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).get must include("/bas-gateway/sign-in")
          redirectLocation(result).get mustNot include("http")
        }

        "there is NoActiveSession and environment mode is Dev" in {
          when(mockEnvironment.mode).thenReturn(Mode.Dev)
          when(mockAppConfig.isDevEnv).thenReturn(true)

          val exception: Throwable   = BearerTokenExpired()
          val result: Future[Result] = Future.successful(sut.resolveError(request, exception))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).get must include("/bas-gateway/sign-in")
          redirectLocation(result).get must include("http")
        }
      }

      "return 403 FORBIDDEN when there are InsufficientEnrolments" in {
        val exception: Throwable   = InsufficientEnrolments()
        val result: Future[Result] = Future.successful(sut.resolveError(request, exception))

        status(result) mustBe FORBIDDEN
      }

      "return ExternalErrorPage with 500 INTERNAL_SERVER_ERROR" when {
        def test(exception: Throwable): Unit =
          s"$exception is thrown" in {
            val result: Future[Result] = Future.successful(sut.resolveError(request, exception))

            status(result) mustBe INTERNAL_SERVER_ERROR
            contentAsString(result) mustBe externalErrorPage()(request, messages).toString
          }

        Seq(
          new Exception("test"),
          new NotFoundException("test"),
          new JsValidationException("test", "test", classOf[String], "errs")
        ).foreach(test)
      }
    }

    ".standardErrorTemplate" must {
      "return error_template" in {
        sut.standardErrorTemplate("pageTitle", "heading", "message") mustBe errorTemplate(
          "pageTitle",
          "heading",
          "message",
          None
        )(request, messages)
      }
    }

    ".onClientError" when {
      "service is shuttered" must {
        "return ShutteringPage with 503 SERVICE_UNAVAILABLE" in {
          when(mockAppConfig.shuttered).thenReturn(true)

          val result: Future[Result] = sut.onClientError(request, SERVICE_UNAVAILABLE, "test")

          status(result) mustBe SERVICE_UNAVAILABLE
          contentAsString(result) mustBe shutteringPage()(request, messages).toString
        }
      }

      "service is unshuttered" must {
        def test(statusCode: Int): Unit =
          s"return $statusCode" in {
            when(mockAppConfig.shuttered).thenReturn(false)

            val result: Future[Result] = sut.onClientError(request, statusCode, "test")

            status(result) mustBe statusCode
          }

        Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach(test)
      }
    }
  }
}
