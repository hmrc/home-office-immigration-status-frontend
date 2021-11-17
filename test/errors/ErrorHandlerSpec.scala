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

package controllers

import play.api.http.Status._
import play.api.test.Helpers.{contentAsString, status}
import views.html.AccessibilityStatementPage
import errors.ErrorHandler
import views.html.InternalErrorPage
import scala.concurrent.Future
import play.api.i18n.{Lang, Messages, MessagesApi}

class ErrorHandlerSpec extends ControllerSpec {

  lazy val sut = inject[ErrorHandler]
  lazy val errorPage = inject[InternalErrorPage]

  "resolveError" must {
    "show the InternalServerError for an unexpected error" in {
      val exception = new Exception("Oh no! Our table! It's broken!")
      val result = Future.successful(sut.resolveError(request, exception))

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustEqual errorPage()(request, messages).toString
    }
  }
}
