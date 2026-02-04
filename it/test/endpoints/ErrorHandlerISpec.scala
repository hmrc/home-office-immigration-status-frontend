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

import play.api.http.Status.NOT_FOUND
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec
import play.api.libs.ws.DefaultBodyReadables.readableAsString

class ErrorHandlerISpec extends ISpec with HomeOfficeImmigrationStatusStubs {

  "GET /check-immigration-status/foo" should {
    "return an error page not found" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = await(request("/foo").get())

      result.status                                       shouldBe NOT_FOUND
      result.body                                           should include("This page can’t be found")
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

}
