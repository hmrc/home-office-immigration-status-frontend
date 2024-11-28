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

package endpoints

import play.api.http.Status.{OK, SEE_OTHER}
import mocks.MockSessionCookie
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedSimpleForm

class SearchByPassportISpec extends ISpec with HomeOfficeImmigrationStatusStubs with MockSessionCookie {

  "GET /check-immigration-status/search-by-passport" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = await(requestWithSession("/search-by-passport", "session-searchByPassportGet").get())

      result.status                                       shouldBe OK
      result.body                                           should include(htmlEscapedMessage("lookup.mrz.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

  "POST /check-immigration-status/search-by-passport" should {
    "redirect to the result page" in {
      givenCheckByMrzSucceeds()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val payload = Map(
        "dateOfBirth.year"  -> "2001",
        "dateOfBirth.month" -> "01",
        "dateOfBirth.day"   -> "31",
        "documentNumber"    -> "123456789",
        "documentType"      -> "PASSPORT",
        "nationality"       -> "AFG"
      )

      val result = await(requestWithSession("/search-by-passport", "session-searchByPassportPost").post(payload))

      result.status                 shouldBe SEE_OTHER
      extractHeaderLocation(result) shouldBe Some(controllers.routes.StatusResultController.onPageLoad.url)
    }
  }
}
