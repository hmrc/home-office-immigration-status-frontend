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

import config.AppConfig
import controllers.ControllerSpec
import org.mockito.Mockito._
import play.api.mvc.BodyParsers
import play.api.{Configuration, Environment}
import support.CallOps
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.ExecutionContext.Implicits.global

class AuthActionSpec extends ControllerSpec with AuthRedirects {

  lazy val config: Configuration       = inject[Configuration]
  lazy val env: Environment            = inject[Environment]
  lazy val connector: AuthConnector    = inject[AuthConnector]
  lazy val parser: BodyParsers.Default = inject[BodyParsers.Default]
  val mockAppConfig: AppConfig         = mock(classOf[AppConfig])

  lazy val sut = new AuthActionImpl(env, connector, mockAppConfig, parser, config)

  override protected def beforeEach(): Unit = {
    reset(mockAppConfig)
    super.beforeEach()
  }

  "getPredicate" must {
    "override stride requirement when config set to ANY" in {
      when(mockAppConfig.authorisedStrideGroup).thenReturn("ANY")
      sut.getPredicate mustEqual AuthProviders(PrivilegedApplication)
    }

    "include stride requirement when config NOT set to ANY" in {
      when(mockAppConfig.authorisedStrideGroup).thenReturn("TBC")
      val expectedResult = Enrolment("TBC") and AuthProviders(PrivilegedApplication)
      sut.getPredicate mustEqual expectedResult
    }
  }

  "handleFailure" should {
    "redirect the user to stride login" in {
      val continueUrl    = CallOps.localFriendlyUrl(env, config)(request.uri, request.host)
      val expectedResult = toStrideLogin(continueUrl)

      sut.handleFailure(request)(new InsufficientConfidenceLevel) mustEqual expectedResult
    }
  }

}
