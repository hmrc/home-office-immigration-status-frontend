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

import controllers.ControllerSpec
import play.api.http.Status._
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import uk.gov.hmrc.auth.core.{BearerTokenExpired, InsufficientEnrolments}
import views.html.{ExternalErrorPage, error_template}

import scala.concurrent.Future

class ErrorHandlerSpec extends ControllerSpec {

  lazy val sut: ErrorHandler             = inject[ErrorHandler]
  lazy val errorPage: ExternalErrorPage  = inject[ExternalErrorPage]
  lazy val errorTemplate: error_template = inject[error_template]

  "resolveError" must {
    "redirect to GGLogin when there is NoActiveSession" in {
      val exception = BearerTokenExpired()
      val result    = Future.successful(sut.resolveError(request, exception))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must include("/bas-gateway/sign-in")
    }

    "return a Forbidden status when there are InsufficientEnrolments" in {
      val exception = InsufficientEnrolments()
      val result    = Future.successful(sut.resolveError(request, exception))

      status(result) mustBe FORBIDDEN
    }

    "show the InternalServerError for an unexpected error" in {
      val exception = new Exception("Oh no! Our table! It's broken!")
      val result    = Future.successful(sut.resolveError(request, exception))

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual errorPage()(request, messages).toString
    }
  }

  "standardErrorTemplate" must {
    "return the error_template" in {
      sut.standardErrorTemplate("pageTitle", "heading", "message") mustEqual errorTemplate(
        "pageTitle",
        "heading",
        "message",
        None
      )(request, messages)

    }
  }

}
