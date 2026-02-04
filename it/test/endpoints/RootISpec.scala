/*
 * Copyright 2026 HM Revenue & Customs
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

package endpoints

import play.api.http.Status.SEE_OTHER
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.ISpec

class RootISpec extends ISpec {

  "GET /check-immigration-status/" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result: WSResponse = await(requestWithSession("/", "session-root").get())

      result.status                 shouldBe SEE_OTHER
      extractHeaderLocation(result) shouldBe Some(controllers.routes.SearchByNinoController.onPageLoad().url)
    }
  }
}
