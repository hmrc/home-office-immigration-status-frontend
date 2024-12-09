/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import controllers.actions.AuthAction
import play.api.Application
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import play.api.test.{FakeRequest, Injecting}
import support.BaseISpec
import uk.gov.hmrc.http.SessionKeys

class AuthActionsISpec extends AuthActionISpecSetup {

  "withAuthorisedWithStrideGroup" should {

    "call body with a valid authProviderId" in {

      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = TestController.test()(request)

      status(result)          shouldBe 200
      contentAsString(result) shouldBe "Passed Auth"
      verifyAuthoriseAttempt()
    }

    "redirect to log in page when user not authenticated" in {
      givenRequestIsNotAuthorised("SessionRecordNotFound")

      val result = TestController.test()(request)
      status(result)             shouldBe 303
      redirectLocation(result).get should include("/stride/sign-in")
      verifyAuthoriseAttempt()
    }

    "redirect to log in page when user authenticated with different provider" in {
      givenRequestIsNotAuthorised("UnsupportedAuthProvider")

      val result = TestController.test()(request)
      status(result)             shouldBe 303
      redirectLocation(result).get should include("/stride/sign-in")
      verifyAuthoriseAttempt()
    }
  }
}

trait AuthActionISpecSetup extends BaseISpec with Injecting {

  override def fakeApplication(): Application = appBuilder.build()

  given request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")

  object TestController {
    val sut: AuthAction = inject[AuthAction]

    def test(): Action[AnyContent] = sut(Ok("Passed Auth"))
  }
}
