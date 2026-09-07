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
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedSimpleForm
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SearchByNinoISpec extends ISpec with HomeOfficeImmigrationStatusStubs with MockSessionCookie {

  override def beforeEach(): Unit = {
    super.beforeEach()
    repository.collection.drop()
    ()
  }

  "GET /check-immigration-status/search-by-nino" must {
    "show the lookup page and have nothing stored in Mongo" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = Await.result(requestWithSession("/search-by-nino", "session-searchByNinoGet").get(), Duration.Inf)

      result.status mustBe OK
      result.body must include(htmlEscapedMessage("lookup.nino.title"))
      result.headers.get("Cache-Control").map(_.mkString) mustBe Some("no-cache, no-store, must-revalidate")
      val actualRepositoryContent =
        await(repository.collection.find(Filters.eq("_id", "nino-searchByPost")).headOption())
      actualRepositoryContent.map(_.id) mustBe None
    }
  }

  "POST /check-immigration-status/search-by-nino" must {
    "redirect to the result page and store ID in Mongo" in {
      givenCheckByNinoSucceeds()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val payload = Map(
        "dateOfBirth.year"  -> "2001",
        "dateOfBirth.month" -> "01",
        "dateOfBirth.day"   -> "31",
        "familyName"        -> "Jane",
        "givenName"         -> "Doe",
        "nino"              -> nino.nino
      )

      val result = await(requestWithSession("/search-by-nino", "nino-searchByPost").post(payload))

      result.status mustBe SEE_OTHER
      extractHeaderLocation(result) mustBe Some(controllers.routes.StatusResultController.onPageLoad.url)

      val actualRepositoryContent =
        await(repository.collection.find(Filters.eq("_id", "nino-searchByPost")).headOption())
      actualRepositoryContent.map(_.id) mustBe Some("nino-searchByPost")
    }
  }
}
