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

package uk.gov.hmrc.homeofficeimmigrationstatus.controllers

import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{redirectLocation, status}

class LandingControllerSpec extends ControllerSpec {

  lazy val sut = inject[LandingController]

  "onPageLoad" must {

    "redirect to check by nino" in {
      val result = sut.onPageLoad(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.StatusCheckByNinoController.onPageLoad.url
    }

    "clear the query from the session" in {
      val requestWithQuery = request.withSession("query" -> "some defined query")
      val result = sut.onPageLoad(requestWithQuery)

      val updatedSession = await(result).session(requestWithQuery)
      updatedSession.get("query") must not be defined
    }
  }

}
