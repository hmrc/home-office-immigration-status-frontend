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

import mocks.MockSessionCookie
import org.mongodb.scala.model.Filters
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedSimpleForm
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class SearchByPassportISpec extends ISpec with HomeOfficeImmigrationStatusStubs with MockSessionCookie {

  override def beforeEach(): Unit = {
    super.beforeEach()
    repository.collection.drop()
    ()
  }

  "GET /check-immigration-status/search-by-passport" must {
    "show the lookup page and have nothing stored in Mongo" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = await(requestWithSession("/search-by-passport", "session-searchByPassportGet").get())

      result.status mustBe OK
      result.body must include(htmlEscapedMessage("lookup.mrz.title"))
      result.headers.get("Cache-Control").map(_.mkString) mustBe Some("no-cache, no-store, must-revalidate")
      val actualRepositoryContent =
        await(repository.collection.find(Filters.eq("_id", "nino-searchByPost")).headOption())
      actualRepositoryContent.map(_.id) mustBe None
    }
  }

  "POST /check-immigration-status/search-by-passport" must {
    "redirect to the result page and store ID in Mongo" in {
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

      result.status mustBe SEE_OTHER
      extractHeaderLocation(result) mustBe Some(controllers.routes.StatusResultController.onPageLoad.url)
      val actualRepositoryContent =
        await(repository.collection.find(Filters.eq("_id", "session-searchByPassportPost")).headOption())
      actualRepositoryContent.map(_.id) mustBe Some("session-searchByPassportPost")
    }
  }
}
