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

package controllers.actions

import config.AppConfig
import controllers.actions.AuthActionSpec.AuthRetrievals
import controllers.ControllerSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc._
import play.api.mvc.Results.Ok
import play.api._
import play.api.test.Helpers.{FORBIDDEN, OK, status}
import support.CallOps
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends ControllerSpec with AuthRedirects {

  def config: Configuration = inject[Configuration]
  def env: Environment      = inject[Environment]

  private lazy val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])
  private lazy val mockAppConfig: AppConfig         = mock(classOf[AppConfig])
  private lazy val parser: BodyParsers.Default      = inject[BodyParsers.Default]

  private lazy val sut: AuthActionImpl = new AuthActionImpl(
    env = env,
    authConnector = mockAuthConnector,
    appConfig = mockAppConfig,
    parser = parser,
    config = config
  )

  private lazy val enrolments: Enrolments = Enrolments(Set(Enrolment(key = "TBC")))
  private lazy val credentials: Credentials = Credentials(
    providerId = "StrideUserId",
    providerType = "PrivilegedApplication"
  )

  override protected def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockAppConfig)
    super.beforeEach()
  }

  private class LoginSetup(credentials: Option[Credentials]) {
    type RetrievalType = Option[Credentials] ~ Enrolments

    when(mockAuthConnector.authorise[RetrievalType](any(), any())(any(), any()))
      .thenReturn(Future.successful(credentials composeRetrievals enrolments))

    val result: Future[Result] = sut(_ => Ok)(request)
  }

  "AuthAction" when {
    "a user is logged in with valid credentials and enrolments" must {
      "return 200 OK" in new LoginSetup(Some(credentials)) {
        status(result) mustBe OK
      }
    }

    "a user tries to login with no credentials" must {
      "return 403 FORBIDDEN" in new LoginSetup(None) {
        status(result) mustBe FORBIDDEN
      }
    }

    ".getPredicate" must {
      "override stride requirement when config set to ANY" in {
        when(mockAppConfig.authorisedStrideGroup).thenReturn("ANY")

        sut.getPredicate mustBe AuthProviders(PrivilegedApplication)
      }

      "include stride requirement when config NOT set to ANY" in {
        when(mockAppConfig.authorisedStrideGroup).thenReturn("TBC")

        val expectedResult: Predicate = Enrolment("TBC") and AuthProviders(PrivilegedApplication)

        sut.getPredicate mustBe expectedResult
      }
    }

    ".handleFailure" must {
      "redirect the user to stride login" in {
        val continueUrl: String    = CallOps.localFriendlyUrl(env, config)(request.uri, request.host)
        val expectedResult: Result = toStrideLogin(continueUrl)

        sut.handleFailure(request)(new InsufficientConfidenceLevel) mustBe expectedResult
      }
    }
  }
}

object AuthActionSpec {
  implicit class AuthRetrievals[A](a: A) {
    def composeRetrievals[B](b: B): ~[A, B] = new ~(a, b)
  }
}
